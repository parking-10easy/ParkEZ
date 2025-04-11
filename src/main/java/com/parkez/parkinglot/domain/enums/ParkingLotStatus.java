package com.parkez.parkinglot.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParkingLotStatus {
    OPEN("영업 중"),
    TEMPORARILY_CLOSED("휴업"),
    CLOSED("폐업");

    private final String description;

    @JsonCreator
    public static ParkingLotStatus from(String value) {
        for (ParkingLotStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new ParkingEasyException(ParkingLotErrorCode.INVALID_PARKING_LOT_STATUS);
    }
}
