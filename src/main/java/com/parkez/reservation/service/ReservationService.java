package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.review.service.ReviewReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final DistributedLockManager distributedLockManager;
    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final ReviewReader reviewReader;

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

            // 이미 해당 시간에 예약이 존재할 경우
            List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
            boolean existed = reservationReader.existsReservationByConditions(parkingZone, request.getStartDateTime(), request.getEndDateTime(), statusList);
            if (existed) {
                throw new ParkingEasyException(ReservationErrorCode.ALREADY_RESERVED);
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

    public void cancelReservation(AuthUser authUser, Long reservationId) {

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

        reservationWriter.cancel(reservation);
    }

    public void expireReservation() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);
        reservationWriter.expire(expiredTime);
    }

    public boolean validateRequestTime (ReservationRequest request) {
        return request.getStartDateTime().isBefore(request.getEndDateTime()) && request.getStartDateTime().isAfter(LocalDateTime.now());
    }
}
