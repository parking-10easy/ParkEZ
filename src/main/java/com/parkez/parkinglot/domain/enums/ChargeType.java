package com.parkez.parkinglot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChargeType {
    PAID("유료"),
    FREE("무료");

    private final String description;
}
