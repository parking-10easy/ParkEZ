package com.parkez.reservation.web;

import com.parkez.common.response.Response;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.service.ReservationFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 나의 예약 내역 조회
    @GetMapping("/v1/users/me/reservations")
    public Response<List<ReservationResponse>> getMyReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = 1L;
        return Response.fromPage(reservationFacadeService.getMyReservations(userId, page, size));
    }
}
