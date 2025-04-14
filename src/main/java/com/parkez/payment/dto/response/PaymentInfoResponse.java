package com.parkez.payment.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class PaymentInfoResponse {

    private String customerEmail;
    private String customerName;
    private String customerMobilePhone;
    private BigDecimal totalPrice;
    private String customerKey;


    @Builder
    public PaymentInfoResponse(String customerEmail, String customerName, String customerMobilePhone, BigDecimal totalPrice, String customerKey) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.customerMobilePhone = customerMobilePhone;
        this.totalPrice = totalPrice;
        this.customerKey = customerKey;
    }
}
