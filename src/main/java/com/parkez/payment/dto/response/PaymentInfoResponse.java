package com.parkez.payment.dto.response;

import com.parkez.payment.domain.entity.Payment;
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
    private BigDecimal price;
    private String customerKey;


    @Builder
    public PaymentInfoResponse(String customerEmail, String customerName, String customerMobilePhone, BigDecimal price, String customerKey) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.customerMobilePhone = customerMobilePhone;
        this.price = price;
        this.customerKey = customerKey;
    }

    public static PaymentInfoResponse of(Payment payment) {
        return PaymentInfoResponse.builder()
                .customerEmail(payment.getUserEmail())
                .customerName(payment.getUserNickName())
                .customerMobilePhone(payment.getUserPhone())
                .price(payment.getPrice())
                .customerKey(String.format("user_%d", payment.getUserId()))
                .build();
    }
}
