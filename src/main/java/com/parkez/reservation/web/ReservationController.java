package com.parkez.reservation.web;

import com.parkez.common.response.Response;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.service.ReservationFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacadeService reservationFacadeService;

    // 예약 생성
    @PostMapping("/v1/reservations")
    public Response<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request
    ) {
        Long userId = 1L;
        return Response.of(reservationFacadeService.createReservation(userId, request));
    }

    // 나의 예약 내역 조회
    @GetMapping("/v1/users/me/reservations")
    public Response<ReservationResponse> getMyReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = 1L;
        return Response.fromPage(reservationFacadeService.getMyReservations(userId, page, size));
    }

    // 나의 예약 단건 조회
    @GetMapping("/v1/users/me/reservations/{reservationId}")
    public Response<ReservationResponse> getMyReservation(
            @PathVariable Long reservationId
    ) {
        Long userId = 1L;
        return Response.of(reservationFacadeService.getMyReservation(userId, reservationId));
    }

    // Owner 본인 소유 주차장의 예약 내역 리스트 조회
    @GetMapping("/v1/users/me/parking-zones/{parkingZoneId}/reservations")
    public Response<ReservationResponse> getOwnerReservations(
            @PathVariable Long parkingZoneId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = 1L;
        return Response.fromPage(reservationFacadeService.getOwnerReservations(userId, parkingZoneId, page, size));
    }

    // 예약 취소
    @DeleteMapping("v1/reservations/{reservationId}")
    public Response<Void> cancelReservation(
            @PathVariable Long reservationId
    ) {
        Long userId = 1L;
        reservationFacadeService.cancelReservation(userId, reservationId);
        return Response.empty();
    }
}
