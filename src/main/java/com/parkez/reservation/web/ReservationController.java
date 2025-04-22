package com.parkez.reservation.web;

import com.parkez.common.aop.CheckMemberStatus;
import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.service.ReservationService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "07. 주차공간 예약 API", description = "주차공간에 대한 예약 API 입니다.")
@Secured(UserRole.Authority.USER)
@CheckMemberStatus
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping("/v1/reservations")
    @Operation(summary = "예약 생성", description = "분산락을 통해 동시성 제어를 적용한 예약 생성 기능입니다.")
    public Response<ReservationResponse> createReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody ReservationRequest request
    ) {
        return Response.of(reservationService.createReservation(authUser, request));
    }

    // 나의 예약 내역 조회
    @GetMapping("/v1/reservations/me")
    @Operation(summary = "나의 예약 다건 조회", description = "나의 예약 다건 조회 기능입니다.")
    public Response<ReservationResponse> getMyReservations(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @ParameterObject PageRequest pageRequest
            ) {
        return Response.fromPage(reservationService.getMyReservations(authUser, pageRequest.getPage(), pageRequest.getSize()));
    }

    // 나의 예약 단건 조회
    @GetMapping("/v1/reservations/me/{reservationId}")
    @Operation(summary = "나의 예약 단건 조회", description = "나의 예약 단건 조회 기능입니다.")
    public Response<ReservationResponse> getMyReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(reservationService.getMyReservation(authUser, reservationId));
    }

    // Owner 본인 소유 주차장의 예약 내역 리스트 조회
    @Secured(UserRole.Authority.OWNER)
    @Operation(summary = "특정 주차공간에 대한 예약 내역 조회", description = "주차공간 소유주의 특정 주차공간에 대한 예약 내역 조회 기능입니다.")
    @GetMapping("/v1/users/me/parking-zones/{parkingZoneId}/reservations")
    public Response<ReservationResponse> getOwnerReservations(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @ParameterObject PageRequest pageRequest
    ) {
        return Response.fromPage(reservationService.getOwnerReservations(authUser, parkingZoneId, pageRequest.getPage(), pageRequest.getSize()));
    }

    // 주차공간 예약 사용 완료(이용 시간 만료)
    @PatchMapping("/v1/reservations/me/{reservationId}")
    @Operation(summary = "예약 사용 완료", description = "사용 완료한 예약에 대한 완료 처리 기능입니다.")
    public Response<Void> completeReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        reservationService.completeReservation(authUser, reservationId);
        return Response.empty();
    }

    // 예약 취소
    @DeleteMapping("v1/reservations/me/{reservationId}")
    @Operation(summary = "예약 취소", description = "사용하지 않은 예약에 대한 취소 기능입니다.")
    public Response<Void> cancelReservation(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request
            ) {
        reservationService.cancelReservation(authUser, reservationId, request);
        return Response.empty();
    }
}
