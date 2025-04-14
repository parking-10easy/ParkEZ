package com.parkez.payment.dto.response;

import com.parkez.payment.domain.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "결제 요청 생성 응답")
public class PaymentCreateResponse {

    private final Long paymentId;

    private final BigDecimal totalPrice;

    private final String orderId;


    @Builder
    public PaymentCreateResponse(Long paymentId, BigDecimal totalPrice, String orderId) {
        this.paymentId = paymentId;
        this.totalPrice = totalPrice;
        this.orderId = orderId;
    }

    public static PaymentCreateResponse of(Long paymentId, BigDecimal totalPrice, String orderId) {
        return PaymentCreateResponse.builder()
                .paymentId(paymentId)
                .totalPrice(totalPrice)
                .orderId(orderId)
                .build();
    }
}
