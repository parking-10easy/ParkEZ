package com.parkez.settlement.web;

import com.parkez.common.aop.CheckMemberStatus;
import com.parkez.common.dto.response.Response;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.settlement.dto.response.SettlementPreviewResponse;
import com.parkez.settlement.dto.response.SettlementReservationResponse;
import com.parkez.settlement.dto.response.SettlementResponse;
import com.parkez.settlement.exception.SettlementErrorCode;
import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/settlement")
@RequiredArgsConstructor
@Tag(name = "정산 API", description = "정산 내역 조회 및 정산 확정 API 입니다.")
@Secured(UserRole.Authority.OWNER)
@CheckMemberStatus
public class SettlementController {

    private final SettlementService settlementService;

    // 월별 예상 정산 내역 조회 API
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/preview")
    @Operation(summary = "월별 정산 프리뷰 조회", description = "현재 날짜를 기준으로 해당 월의 예상 정산 내역을 조회합니다.")
    public Response<SettlementPreviewResponse> getSettlementPreview(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser
    ) {
        YearMonth now = YearMonth.now(); // 내부에서 자동 처리

        return Response.of(settlementService.getPreview(authUser, now));
    }

    // 예약 건에 대한 정산 내역 조회 API
    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 건에 대한 정산 내역 조회", description = "owner가 지정한 예약 건을 기준으로 정산내역을 조회합니다.")
    public Response<SettlementReservationResponse> getReservationSettlement(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ) {
        return Response.of(settlementService.getReservationSettlement(authUser, reservationId));

    }

    // 정산 완료 처리 API
    @PatchMapping("/{settlementId}/complete")
    @Secured(UserRole.Authority.OWNER)
    @Operation(summary = "정산 완료 처리", description = "정산 상태를 COMPLETED로 변경합니다.")
    public Response<Void> complete(@PathVariable Long settlementId) {
        settlementService.completeSettlement(settlementId);
        return Response.empty();
    }

    // 확정 정산 내역 조회 API
    @GetMapping
    @Secured(UserRole.Authority.OWNER)
    @Operation(summary = "확정 정산 내역 조회", description = "지정한 연월 기준으로 정산 정보를 확인합니다.")
    public Response<SettlementResponse> getConfirmedSettlement(
            @AuthenticatedUser AuthUser authUser,
            @RequestParam int year,
            @RequestParam int month
    ) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw new ParkingEasyException(SettlementErrorCode.INVALID_YEAR_MONTH);
        }
        return Response.of(settlementService.getConfirmedSettlement(authUser, yearMonth));
    }

}