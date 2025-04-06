package com.parkez.reservation.dto.response;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReservationResponse {

    private final Long ReservationId;
    private final Long userId;
    private final Long parkingZoneId;
    private final String parkingLotName;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final BigDecimal price;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getParkingZone().getId(),
                reservation.getParkingLotName(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
