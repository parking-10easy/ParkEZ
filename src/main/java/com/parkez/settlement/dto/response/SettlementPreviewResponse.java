package com.parkez.settlement.dto.response;

import com.parkez.settlement.domain.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Schema(description = "소유자(나)의 예상 정산 내역 조회 응답 dto")
public class SettlementPreviewResponse {

    private YearMonth settlementMonth;
    private BigDecimal totalAmount;
    private BigDecimal totalFee;
    private BigDecimal netAmount;
    private Long reservationCount;
    private SettlementStatus status;

    @Builder
    public SettlementPreviewResponse(YearMonth settlementMonth, BigDecimal totalAmount, BigDecimal totalFee, BigDecimal netAmount, Long reservationCount, SettlementStatus status) {
        this.settlementMonth = settlementMonth;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.netAmount = netAmount;
        this.reservationCount = reservationCount;
        this.status = status;
    }

    public static SettlementPreviewResponse of(YearMonth settlementMonth, BigDecimal totalAmount, BigDecimal totalFee, BigDecimal netAmount, Long reservationCount){
        return SettlementPreviewResponse.builder()
                .settlementMonth(settlementMonth)
                .totalAmount(totalAmount)
                .totalFee(totalFee)
                .netAmount(netAmount)
                .reservationCount(reservationCount)
                .status(SettlementStatus.PENDING)
                .build();

    }

}
