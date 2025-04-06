package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneQueryService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
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

@Service
@RequiredArgsConstructor
public class ReservationFacadeService {

    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserQueryService userQueryService;
    private final ParkingZoneQueryService parkingZoneQueryService;

    public ReservationResponse createReservation(Long userId, ReservationRequest request) {

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

        return ReservationResponse.from(reservation);
    }

    public Page<ReservationResponse> getMyReservations(Long userId, int page, int size) {

        int adjustedPage = (page > 0) ? page - 1 : 0;
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by("createdAt").descending());
        Page<Reservation> pageMyReservations = reservationReader.findMyReservations(userId, pageable);

        return pageMyReservations.map(ReservationResponse::from);
    }

    public ReservationResponse getMyReservation(Long userId, Long reservationId) {

        Reservation myReservation = reservationReader.findReservation(userId, reservationId);

        return ReservationResponse.from(myReservation);
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
