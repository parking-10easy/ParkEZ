package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneQueryService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.review.service.ReviewQueryService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservationFacadeService {

    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserQueryService userQueryService;
    private final ParkingZoneQueryService parkingZoneQueryService;
    private final ReviewQueryService reviewQueryService;

    public MyReservationResponse createReservation(Long userId, ReservationRequest request) {

        User user = userQueryService.findById(userId);
        ParkingZone parkingZone = parkingZoneQueryService.findById(request.getParkingZoneId());

        // 요금 계산
        long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
        if (hours <= 0) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }
        BigDecimal price = parkingZone.getParkingLot().getPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = reservationWriter.createReservation(
                user,
                parkingZone,
                parkingZone.getParkingLot().getName(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                price
        );

        boolean isReviewWritten = false;

        return MyReservationResponse.of(reservation, isReviewWritten);
    }

    public Page<MyReservationResponse> getMyReservations(Long userId, int page, int size) {

        int adjustedPage = (page > 0) ? page - 1 : 0;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<Reservation> pageMyReservations = reservationReader.findMyReservations(userId, pageable);

        // 리뷰 작성 여부 조회
        List<Long> reservationIds = pageMyReservations.getContent().stream().map(Reservation::getId).toList();
        Set<Long> reviewedIds = reviewQueryService.findReviewedReservationIds(reservationIds);

        return pageMyReservations.map(reservation ->
                MyReservationResponse.of(reservation, reviewedIds.contains(reservation.getId()))
        );
    }

    public MyReservationResponse getMyReservation(Long userId, Long reservationId) {

        Reservation myReservation = reservationReader.findReservation(userId, reservationId);

        // 리뷰 작성 여부 조회
        boolean isReviewWritten = reviewQueryService.isReviewWritten(myReservation.getId());

        return MyReservationResponse.of(myReservation, isReviewWritten);
    }

    public Page<ReservationResponse> getOwnerReservations(Long userId, Long parkingZoneId, int page, int size) {

        // 조회하려는 주차공간이 없는 주차공간일 경우 예외
        if (!parkingZoneQueryService.existsById(parkingZoneId)) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_FOUND_PARKING_ZONE);
        }

        // 조회하려는 주차공간이 본인 소유의 주차공간이 아닐 경우 예외
        if (!parkingZoneQueryService.findById(parkingZoneId).getParkingLot().getOwner().getId().equals(userId)) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_MY_PARKING_ZONE);
        }

        int adjustedPage = (page > 0) ? page - 1 : 0;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<Reservation> pageReservations = reservationReader.findOwnerReservations(parkingZoneId, pageable);

        return pageReservations.map(ReservationResponse::from);
    }

    public void completeReservation(Long userId, Long reservationId) {

        Reservation reservation = reservationReader.findReservation(userId, reservationId);

        // 예약 완료 됨 상태의 예약만 사용 완료 됨으로 변경 가능 예외
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS);
        }

        reservationWriter.complete(reservation);
    }

    public void cancelReservation(Long userId, Long reservationId) {

        Reservation reservation = reservationReader.findReservation(userId, reservationId);

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
