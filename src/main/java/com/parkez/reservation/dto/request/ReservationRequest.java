package com.parkez.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationRequest {

    @NotNull
    private Long parkingZoneId;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
}
