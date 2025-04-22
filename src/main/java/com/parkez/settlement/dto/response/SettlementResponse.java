package com.parkez.settlement.dto.response;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@RequiredArgsConstructor
@Schema(description = "확정 정산 내역 조회 응답 dto")
public class SettlementResponse {

    private YearMonth settlementMonth;
    private BigDecimal totalAmount;
    private BigDecimal totalFee;
    private BigDecimal netAmount;
    private SettlementStatus status;
    private LocalDateTime calculatedAt;
    private LocalDateTime settledAt;

    @Builder
    public SettlementResponse(YearMonth settlementMonth, BigDecimal totalAmount, BigDecimal totalFee, BigDecimal netAmount, SettlementStatus status, LocalDateTime calculatedAt, LocalDateTime settledAt) {
        this.settlementMonth = settlementMonth;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.netAmount = netAmount;
        this.status = status;
        this.calculatedAt = calculatedAt;
        this.settledAt = settledAt;
    }

    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.builder()
                .settlementMonth(settlement.getSettlementMonth())
                .totalAmount(settlement.getTotalAmount())
                .totalFee(settlement.getTotalFee())
                .netAmount(settlement.getNetAmount())
                .status(settlement.getStatus())
                .calculatedAt(settlement.getCalculatedAt())
                .settledAt(settlement.getSettledAt())
                .build();
    }
}
