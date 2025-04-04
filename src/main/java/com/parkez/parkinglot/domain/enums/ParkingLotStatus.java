package com.parkez.parkinglot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParkingLotStatus {
    OPEN("영업 중"),
    TEMPORARILY_CLOSED("휴업"),
    CLOSED("폐업");

    private final String description;
}
