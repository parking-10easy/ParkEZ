package com.parkez.reservation.web;

import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.response.Response;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.OwnerReservationResponse;
import com.parkez.reservation.service.ReservationService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주차공간 예약 API", description = "주차공간에 대한 예약 API 입니다.")
public class ReservationController {

    private final ReservationService reservationFacadeService;

    // 예약 생성
    @Secured(UserRole.Authority.USER)
    @PostMapping("/v1/reservations")
    public Response<MyReservationResponse> createReservation(
            @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody ReservationRequest request
    ) {
        return Response.of(reservationFacadeService.createReservation(authUser, request));
    }

    // 나의 예약 내역 조회
    @GetMapping("/v1/reservations/me")
    public Response<MyReservationResponse> getMyReservations(
            @AuthenticatedUser AuthUser authUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return Response.fromPage(reservationFacadeService.getMyReservations(authUser, page, size));
    }

    // 나의 예약 단건 조회
    @GetMapping("/v1/reservations/me/{reservationId}")
    public Response<MyReservationResponse> getMyReservation(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(reservationFacadeService.getMyReservation(authUser, reservationId));
    }

    // Owner 본인 소유 주차장의 예약 내역 리스트 조회
    @GetMapping("/v1/users/me/parking-zones/{parkingZoneId}/reservations")
    public Response<OwnerReservationResponse> getOwnerReservations(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return Response.fromPage(reservationFacadeService.getOwnerReservations(authUser, parkingZoneId, page, size));
    }

    // 주차공간 예약 사용 완료(이용 시간 만료)
    @PatchMapping("/v1/reservations/me/{reservationId}")
    public Response<Void> completeReservation(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationFacadeService.completeReservation(authUser, reservationId);
        return Response.empty();
    }

    // 예약 취소
    @DeleteMapping("v1/reservations/me/{reservationId}")
    public Response<Void> cancelReservation(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationFacadeService.cancelReservation(authUser, reservationId);
        return Response.empty();
    }
}
