package com.parkez.parkingzone.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneStatus {

    AVAILABLE("이용 가능"),
    UNAVAILABLE("이용 불가능");

    private final String description;

}
