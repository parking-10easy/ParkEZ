package com.parkez.settlement.dto.response;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@RequiredArgsConstructor
@Schema(description = "확정 정산 내역 조회 응답 dto")
public class SettlementResponse {

    private final YearMonth settlementMonth;
    private final BigDecimal totalAmount;
    private final BigDecimal totalFee;
    private final BigDecimal netAmount;
    private final SettlementStatus status;
    private final LocalDateTime calculatedAt;
    private final LocalDateTime settledAt;

    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getSettlementMonth(),
                settlement.getTotalAmount(),
                settlement.getTotalFee(),
                settlement.getNetAmount(),
                settlement.getStatus(),
                settlement.getCalculatedAt(),
                settlement.getSettledAt());
    }
}
