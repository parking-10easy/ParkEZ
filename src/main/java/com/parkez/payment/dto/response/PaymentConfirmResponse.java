package com.parkez.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parkez.payment.domain.enums.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentConfirmResponse {

    @JsonProperty("paymentKey")
    private String paymentKey;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("method")
    private String method;

    @JsonProperty("status")
    private String status;

    @JsonProperty("approvedAt")
    private String approvedAt;

    @JsonProperty("type")
    private String type;
}

