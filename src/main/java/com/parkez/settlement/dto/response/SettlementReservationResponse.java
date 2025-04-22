package com.parkez.settlement.dto.response;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.settlement.domain.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Schema(description = "예약 건에 대한 정산 내역 조회 응답 dto")
public class SettlementReservationResponse {

    private Long reservationId;
    private String orderId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal netAmount;
    private LocalDateTime paidAt;
    private SettlementStatus status;

    @Builder
    public SettlementReservationResponse(Long reservationId, String orderId, BigDecimal amount, BigDecimal fee, BigDecimal netAmount, LocalDateTime paidAt, SettlementStatus status) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount;
        this.paidAt = paidAt;
        this.status = status;
    }

    public static SettlementReservationResponse of(Payment payment, double feeRate, SettlementStatus status) {
        BigDecimal amount = payment.getPrice();
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(feeRate));
        BigDecimal netAmount = amount.subtract(fee);

        return SettlementReservationResponse.builder()
                .reservationId(payment.getReservation().getId())
                .orderId(payment.getOrderId())
                .amount(amount)
                .fee(fee)
                .netAmount(netAmount)
                .paidAt(payment.getApprovedAt())
                .status(status)
                .build();
    }
}
