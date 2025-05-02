package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "결제 승인 요청")
public class PaymentConfirmRequest {

    @Schema(description = "paymentKey", example = "paymentKey")
    @NotBlank
    private String paymentKey;

    @Schema(description = "주문번호", example = "orderId")
    @NotBlank
    private String orderId;

    @Schema(description = "결제금액", example = "결제금액")
    @NotNull
    private Integer amount;
}
