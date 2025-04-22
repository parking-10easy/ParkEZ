package com.parkez.settlement.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {

    PENDING("예정 정산"),
    CONFIRMED("정산 확정"),
    COMPLETED("정산 완료");

    private final String description;

}