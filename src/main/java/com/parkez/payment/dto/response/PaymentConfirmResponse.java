package com.parkez.payment.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class PaymentConfirmResponse {

    private String paymentKey;

    private String orderId;

    private Integer amount;

    private String method;

    private String status;

    private String approvedAt;

    private String type;
}

