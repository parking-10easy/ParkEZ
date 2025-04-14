package com.parkez.reservation.dto.response;

import com.parkez.reservation.domain.entity.Reservation;

public record ReservationWithReviewDto(Reservation reservation, boolean reviewWritten) {
}
