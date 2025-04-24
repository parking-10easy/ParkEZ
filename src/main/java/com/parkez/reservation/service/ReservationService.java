package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.payment.service.PaymentService;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final DistributedLockManager distributedLockManager;
    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final ReviewReader reviewReader;
    private final PaymentService paymentService;
    private final QueueService queueService;

    private static final long CANCEL_LIMIT_HOURS = 1L;
    private static final long EXPIRATION_TIME = 10L;

    public ReservationResponse createReservation(AuthUser authUser, ReservationRequest request) {

        return distributedLockManager.executeWithLock(request.getParkingZoneId(), () -> {

            User user = userReader.getActiveUserById(authUser.getId());
            ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(request.getParkingZoneId());

            // 예약 날짜 및 시간 입력 오류 예외
            if (!validateRequestTime(request)) {
                throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
            }

            // parkingZone 의 상태가 AVAILABLE 일 경우에만 예약 가능
            if (!parkingZone.getStatus().equals(ParkingZoneStatus.AVAILABLE)) {
                throw new ParkingEasyException(ReservationErrorCode.CANT_RESERVE_UNAVAILABLE_PARKING_ZONE);
            }

            // parkingLot 의 영업 시간 내에만 예약 가능
            if (!parkingZone.isOpened(request.getStartDateTime(), request.getEndDateTime())) {
                throw new ParkingEasyException(ReservationErrorCode.CANT_RESERVE_AT_CLOSE_TIME);
            }

            // 이미 해당 시간에 예약이 존재할 경우 -> 대기열에 추가
            List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
            boolean existed = reservationReader.existsReservationByConditions(parkingZone, request.getStartDateTime(), request.getEndDateTime(), statusList);

            if (existed) {
                JoinQueueResult result = queueService.joinWaitingQueue(user.getId(), request);

                switch (result) {
                    case JOINED -> throw new ParkingEasyException(QueueErrorCode.JOINED_WAITING_QUEUE); // 대기열 저장 성공
                    case ALREADY_JOINED -> throw new ParkingEasyException(QueueErrorCode.ALREADY_IN_QUEUE); // 이미 사용자가 대기열에 존재
                }

            }

            Reservation reservation = reservationWriter.create(
                    user,
                    parkingZone,
                    request.getStartDateTime(),
                    request.getEndDateTime()
            );

            return ReservationResponse.from(reservation);
        });
    }

    public Page<ReservationResponse> getMyReservations(AuthUser authUser, int page, int size) {

        int adjustedPage = page -1;
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

        int adjustedPage = page -1;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<Reservation> pageReservations = reservationReader.findOwnerReservations(parkingZoneId, pageable);

        return pageReservations.map(ReservationResponse::from);
    }

    public void completeReservation(AuthUser authUser, Long reservationId) {

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        // 예약 완료 됨 상태의 예약만 사용 완료 됨으로 변경 가능 예외
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS);
        }

        reservationWriter.complete(reservation);
    }

    public void cancelReservation(AuthUser authUser, Long reservationId, ReservationCancelRequest request) {

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        // 결제 대기 중 또는 결제 완료 된 예약만 취소할 수 있음
        if (!reservation.canBeCanceled()) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_RESERVATION);
        }

        // 시작 시간 1시간 이내일 경우 취소 불가 예외
        LocalDateTime cancelLimitTime = reservation.getStartDateTime().minusHours(CANCEL_LIMIT_HOURS);
        if (reservation.isAfter(cancelLimitTime, LocalDateTime.now())) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR);
        }

        paymentService.cancelPayment(reservation, request);

        reservationWriter.cancel(reservation);

        handleNextInQueue(reservation);
    }

    public void expireReservation() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);
        reservationWriter.expire(expiredTime);
    }

    public boolean validateRequestTime (ReservationRequest request) {
        LocalDateTime startDateTime = request.getStartDateTime();
        LocalDateTime endDateTime = request.getEndDateTime();

        return startDateTime.isBefore(endDateTime)
                && startDateTime.isAfter(LocalDateTime.now())
                && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
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

        reservationWriter.create(
                user,
                parkingZone,
                request.getStartDateTime(),
                request.getEndDateTime()
        );

    }

}
