package com.parkez.reservation.web;

import com.parkez.common.response.Response;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.service.ReservationFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacadeService reservationFacadeService;

    @PostMapping("/v1/reservations")
    public Response<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request
    ) {
        Long userId = 1L;
        return Response.of(reservationFacadeService.createReservation(userId, request));
    }
}
