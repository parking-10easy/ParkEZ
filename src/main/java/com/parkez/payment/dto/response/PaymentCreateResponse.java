package com.parkez.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "결제 요청 생성 응답")
public class PaymentCreateResponse {

    @Schema(description = "결제 id", example = "1")
    private final Long paymentId;

    @Schema(description = "총 금액", example = "5000")
    private final BigDecimal price;

    @Schema(description = "토스에서 사용되는 주문 id", example = "a7f16e260bd1408a9de472d3d022b983")
    private final String orderId;


    @Builder
    public PaymentCreateResponse(Long paymentId, BigDecimal price, String orderId) {
        this.paymentId = paymentId;
        this.price = price;
        this.orderId = orderId;
    }

    public static PaymentCreateResponse of(Long paymentId, BigDecimal price, String orderId) {
        return PaymentCreateResponse.builder()
                .paymentId(paymentId)
                .price(price)
                .orderId(orderId)
                .build();
    }
}
