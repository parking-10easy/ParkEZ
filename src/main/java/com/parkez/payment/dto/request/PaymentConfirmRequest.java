package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "결제 승인 요청")
public class PaymentConfirmRequest {

    private String paymentKey;
    private String orderId;
    private Integer amount;
}
