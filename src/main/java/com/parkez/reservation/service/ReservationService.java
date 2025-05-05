package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.payment.service.PaymentService;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.excption.PromotionIssueErrorCode;
import com.parkez.promotion.service.PromotionIssueReader;
import com.parkez.promotion.service.PromotionIssueValidator;
import com.parkez.promotion.service.PromotionIssueWriter;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.dto.WaitingUserDto;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.queue.redis.QueueKey;
import com.parkez.queue.service.QueueService;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.review.service.ReviewReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

	private final DistributedLockManager distributedLockManager;
	private final ReservationReader reservationReader;
	private final ReservationWriter reservationWriter;
	private final UserReader userReader;
	private final ParkingZoneReader parkingZoneReader;
	private final ReviewReader reviewReader;
	private final PaymentService paymentService;
	private final PromotionIssueReader promotionIssueReader;
	private final PromotionIssueValidator promotionIssueValidator;
	private final PromotionIssueWriter promotionIssueWriter;
    private final QueueService queueService;
	private final ReservationProcessor reservationProcessor;

	private static final long CANCEL_LIMIT_HOURS = 1L;
	private static final long EXPIRATION_TIME = 10L;

	public ReservationResponse createReservation(AuthUser authUser, ReservationRequest request, LocalDateTime now) {
		try {
			return distributedLockManager.executeWithLock(request.getParkingZoneId(), () -> reservationProcessor.create(authUser, request, now));
		} catch (ParkingEasyException e) {
			if (e.getErrorCode() == ReservationErrorCode.RESERVATION_LOCK_FAILED) {
				// 락 선점 실패 → 대기열 등록은 완료했으므로 예약 생성 결과 데이터는 null 반환
				handleQueueOnLockFail(authUser, request);
				return null;
			}
			throw e;
		}
	}

	public Page<ReservationResponse> getMyReservations(AuthUser authUser, int page, int size) {

		int adjustedPage = page - 1;
		PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
		Page<ReservationWithReviewDto> pageDto = reservationReader.findMyReservations(authUser.getId(), pageable);

		return pageDto.map(dto ->
			ReservationResponse.of(dto.reservation(), dto.reviewWritten())
		);
	}

	public ReservationResponse getMyReservation(AuthUser authUser, Long reservationId) {

		Reservation myReservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

		// 리뷰 작성 여부 조회
		boolean reviewWritten = reviewReader.isReviewWritten(myReservation.getId());

		return ReservationResponse.of(myReservation, reviewWritten);
	}

	public Page<ReservationResponse> getOwnerReservations(AuthUser authUser, Long parkingZoneId, int page, int size) {

		ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(parkingZoneId);

		// 조회하려는 주차공간이 본인 소유의 주차공간이 아닐 경우 예외
		if (!parkingZone.getParkingLot().isOwned(authUser.getId())) {
			throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
		}

		int adjustedPage = page - 1;
		PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
		Page<Reservation> pageReservations = reservationReader.findOwnerReservations(parkingZoneId, pageable);

		return pageReservations.map(ReservationResponse::from);
	}

	@Transactional
	public void completeReservation(AuthUser authUser, Long reservationId) {

		Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

		// 예약 완료 됨 상태의 예약만 사용 완료 됨으로 변경 가능 예외
		if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
			throw new ParkingEasyException(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS);
		}

		reservationWriter.complete(reservation);
	}

	@Transactional
	public void cancelReservation(AuthUser authUser, Long reservationId, ReservationCancelRequest request,
		LocalDateTime now) {

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        // 결제 완료 된 예약만 취소할 수 있음
        if (!reservation.canBeCanceled()) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_RESERVATION);
        }

        // 시작 시간 1시간 이내일 경우 취소 불가 예외
        LocalDateTime cancelLimitTime = reservation.getStartDateTime().minusHours(CANCEL_LIMIT_HOURS);
        if (reservation.isAfter(cancelLimitTime, LocalDateTime.now())) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR);
        }

        if (reservation.getPromotionIssueId() != null) {
            PromotionIssue promotionIssue = promotionIssueReader.getById(reservation.getPromotionIssueId());
            if (!promotionIssue.isExpired(now)) {
                promotionIssueWriter.cancelUsage(promotionIssue);
            }
        }

        paymentService.cancelPayment(reservation, request);

        reservationWriter.cancel(reservation);

        handleNextInQueue(reservation);
    }

	public void expireReservation() {
		LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);
		List<Reservation> expiredReservations = reservationWriter.expire(expiredTime);

		for (Reservation reservation : expiredReservations) {
			handleNextInQueue(reservation);
		}
	}

	public boolean validateRequestTime(ReservationRequest request) {
		LocalDateTime startDateTime = request.getStartDateTime();
		LocalDateTime endDateTime = request.getEndDateTime();

		return startDateTime.isBefore(endDateTime)
			&& startDateTime.isAfter(LocalDateTime.now())
			&& startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
	}

	private long calculateUsedHour(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		return ChronoUnit.HOURS.between(startDateTime, endDateTime);
	}

    private void handleNextInQueue(Reservation reservation) {
        String key = QueueKey.generateKey(
                reservation.getParkingZoneId(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime()
        );

        WaitingUserDto dto = queueService.dequeueConvertToDto(key);
        log.info("[대기열] ReservationService에서 받은 WaitingUserDto ={}", dto);
        if (dto == null) {
            log.info("[대기열] 대기자 없음 → key={}", key);
            return;
        }

        User user = userReader.getActiveUserById(dto.getUserId());

        ReservationRequest request = new ReservationRequest(
                dto.getParkingZoneId(),
                dto.getReservationStartDateTime(),
                dto.getReservationEndDateTime()
        );

        createFromQueue(user, request);
        log.info("[대기열] 대기자 예약 확정 완료 → userId={}", user.getId()); // todo 메일 전송
    }

    // 대기열에 있는 사용자의 예약 생성 : 락 적용이 필요없기 때문에 메서드 분리함
    public void createFromQueue(User user, ReservationRequest request) {
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(request.getParkingZoneId());

		long hours = calculateUsedHour(request.getStartDateTime(), request.getEndDateTime());

		BigDecimal originalPrice = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

        reservationWriter.create(
                user,
                parkingZone,
                request.getStartDateTime(),
                request.getEndDateTime(),
				originalPrice,
				BigDecimal.ZERO,
				originalPrice,
                null
        );

    }

	private void handleJoinQueue(User user, ReservationRequest request) {
		JoinQueueResult result = queueService.joinWaitingQueue(user.getId(), request);

		if (result == JoinQueueResult.ALREADY_JOINED) {
			throw new ParkingEasyException(QueueErrorCode.ALREADY_IN_QUEUE);
		}
	}

	private void handleQueueOnLockFail(AuthUser authUser, ReservationRequest request) {
		User user = userReader.getActiveUserById(authUser.getId());

		JoinQueueResult result = queueService.joinWaitingQueue(user.getId(), request);

		if (result == JoinQueueResult.ALREADY_JOINED) {
			throw new ParkingEasyException(QueueErrorCode.ALREADY_IN_QUEUE);
		}
	}
}
