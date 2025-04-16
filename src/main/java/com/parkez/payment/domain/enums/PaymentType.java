package com.parkez.payment.domain.enums;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.payment.exception.PaymentErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentType {

    NORMAL("일반결제"),
    BILLING("자동결제"),
    BRANDPAY("브랜드페이");

    private final String description;

    public static PaymentType from(String value) {
        return Arrays.stream(PaymentType.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new ParkingEasyException(PaymentErrorCode.ILLEGAL_PAYMENT_TYPE));
    }
}
