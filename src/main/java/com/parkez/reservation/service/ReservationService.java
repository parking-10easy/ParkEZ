package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.OwnerReservationResponse;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.reservation.service.jpaconcurrency.ReservationLockService;
import com.parkez.review.service.ReviewReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service("default")
@RequiredArgsConstructor
public class ReservationService implements ReservationLockService {

    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final ReviewReader reviewReader;

    @Override
    public MyReservationResponse createReservation(AuthUser authUser, ReservationRequest request) {

        User user = userReader.getActiveById(authUser.getId());
        ParkingZone parkingZone = parkingZoneReader.findById(request.getParkingZoneId());

        // 요금 계산
        long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
        if (hours <= 0) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }
        BigDecimal price = parkingZone.extractParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = reservationWriter.createReservation(
                user,
                parkingZone,
                parkingZone.extractParkingLotName(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                price
        );

        boolean reviewWritten = false;

        return MyReservationResponse.of(reservation, reviewWritten);
    }

    public Page<MyReservationResponse> getMyReservations(AuthUser authUser, int page, int size) {

        int adjustedPage = (page > 0) ? page - 1 : 0;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<ReservationWithReviewDto> pageDto = reservationReader.findMyReservations(authUser.getId(), pageable);

        return pageDto.map(dto ->
                MyReservationResponse.of(dto.reservation(), dto.reviewWritten())
        );
    }

    public MyReservationResponse getMyReservation(AuthUser authUser, Long reservationId) {

        Reservation myReservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        // 리뷰 작성 여부 조회
        boolean reviewWritten = reviewReader.isReviewWritten(myReservation.getId());

        return MyReservationResponse.of(myReservation, reviewWritten);
    }

    public Page<OwnerReservationResponse> getOwnerReservations(AuthUser authUser, Long parkingZoneId, int page, int size) {

        // 조회하려는 주차공간이 없는 주차공간일 경우 예외
        if (!parkingZoneReader.existsById(parkingZoneId)) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_FOUND_PARKING_ZONE);
        }

        // 조회하려는 주차공간이 본인 소유의 주차공간이 아닐 경우 예외
        ParkingZone parkingZone = parkingZoneReader.findById(parkingZoneId);
        if (!parkingZone.isOwnedBy(authUser.getId())) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_MY_PARKING_ZONE);
        }

        int adjustedPage = (page > 0) ? page - 1 : 0;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<Reservation> pageReservations = reservationReader.findOwnerReservations(parkingZoneId, pageable);

        return pageReservations.map(OwnerReservationResponse::from);
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

        // 사용 완료 된 예약 또는 이미 취소된 예약은 취소 불가 예외
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_COMPLETED_RESERVATION);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_CANCELED_RESERVATION);
        }

        // 시작 시간 1시간 이내일 경우 취소 불가 예외
        if (reservation.getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR);
        }

        reservationWriter.cancel(reservation);
    }
}
