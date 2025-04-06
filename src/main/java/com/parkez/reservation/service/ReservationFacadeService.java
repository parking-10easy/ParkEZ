package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneQueryService;
import com.parkez.reservation.domain.entity.Reservation;
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

        Reservation myReservation = reservationReader.findMyReservation(userId, reservationId);

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
}
