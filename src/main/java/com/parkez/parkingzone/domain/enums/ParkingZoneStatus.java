package com.parkez.parkingzone.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneStatus {

    AVAILABLE("이용 가능"),
    UNAVAILABLE("이용 불가능");

    private final String description;

    @JsonCreator
    public static ParkingZoneStatus from(String value) {
        for (ParkingZoneStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new ParkingEasyException(ParkingZoneErrorCode.INVALID_PARKING_ZONE_STATUS);
    }
}
