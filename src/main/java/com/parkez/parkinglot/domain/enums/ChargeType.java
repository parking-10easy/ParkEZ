package com.parkez.parkinglot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChargeType {
    NO_DATA("정보가 없습니다"),
    PAID("유료"),
    FREE("무료");

    private final String description;
}
