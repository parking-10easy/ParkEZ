package com.parkez.reservation.dto.response;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MyReservationResponse {

    private final Long ReservationId;
    private final Long userId;
    private final Long parkingZoneId;
    private final String parkingLotName;
    private final boolean isReviewWritten;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final BigDecimal price;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    public static MyReservationResponse of(Reservation reservation, boolean isReviewWritten) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getParkingZone().getId(),
                reservation.getParkingLotName(),
                isReviewWritten,
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
