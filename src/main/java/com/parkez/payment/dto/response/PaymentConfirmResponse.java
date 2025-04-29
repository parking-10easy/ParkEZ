package com.parkez.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parkez.payment.domain.enums.PaymentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class PaymentConfirmResponse {

    private String paymentKey;

    private String orderId;

    @JsonProperty("totalAmount")
    private Integer amount;

    private String approvedAt;

    private PaymentType type;

}

