package com.parkez.reservation.web;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.dto.response.Response;
import com.parkez.reservation.domain.enums.LockStrategy;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.OwnerReservationResponse;
import com.parkez.reservation.service.ReservationService;
import com.parkez.reservation.service.jpaconcurrency.ReservationLockService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주차공간 예약 API", description = "주차공간에 대한 예약 API 입니다.")
public class ReservationController {

    private final ReservationService reservationService;
    private final Map<String, ReservationLockService> reservationLockServiceMap;

    // 락 사용 x OR 낙관적 락 OR 비관적 락을 통한 예약 생성
    @Secured(UserRole.Authority.USER)
    @PostMapping("/v1/reservations/{strategy}")
    @Operation(summary = "예약 생성", description = "락 사용 x OR 낙관적 락 OR 비관적 락을 통한 예약 생성 기능입니다.")
    public Response<MyReservationResponse> createReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Parameter(description = "동시성 제어 전략", example = "default")
            @PathVariable LockStrategy strategy,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationLockService service = reservationLockServiceMap.get(strategy.name().toLowerCase());
        return Response.of(service.createReservation(authUser, request));
    }

    // 나의 예약 내역 조회
    @Secured(UserRole.Authority.USER)
    @GetMapping("/v1/reservations/me")
    @Operation(summary = "나의 예약 다건 조회", description = "나의 예약 다건 조회 기능입니다.")
    public Response<MyReservationResponse> getMyReservations(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Parameter PageRequest pageRequest
            ) {
        return Response.fromPage(reservationService.getMyReservations(authUser, pageRequest.getPage(), pageRequest.getSize()));
    }

    // 나의 예약 단건 조회
    @Secured(UserRole.Authority.USER)
    @GetMapping("/v1/reservations/me/{reservationId}")
    public Response<MyReservationResponse> getMyReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(reservationService.getMyReservation(authUser, reservationId));
    }

    // Owner 본인 소유 주차장의 예약 내역 리스트 조회
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/v1/users/me/parking-zones/{parkingZoneId}/reservations")
    public Response<OwnerReservationResponse> getOwnerReservations(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Parameter PageRequest pageRequest
    ) {
        return Response.fromPage(reservationService.getOwnerReservations(authUser, parkingZoneId, pageRequest.getPage(), pageRequest.getSize()));
    }

    // 주차공간 예약 사용 완료(이용 시간 만료)
    @Secured(UserRole.Authority.USER)
    @PatchMapping("/v1/reservations/me/{reservationId}")
    public Response<Void> completeReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationService.completeReservation(authUser, reservationId);
        return Response.empty();
    }

    // 예약 취소
    @Secured(UserRole.Authority.USER)
    @DeleteMapping("v1/reservations/me/{reservationId}")
    public Response<Void> cancelReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(authUser, reservationId);
        return Response.empty();
    }
}
