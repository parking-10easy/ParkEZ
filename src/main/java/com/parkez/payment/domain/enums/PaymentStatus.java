package com.parkez.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("결제 대기 중"),
    APPROVED("결제 승인"),
    CANCELED("결제 취소");

    private final String description;

}
