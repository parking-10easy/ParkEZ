package com.parkez.image.enums;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.image.exception.ImageErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ImageTargetType {

    USER_PROFILE("UserProfile"),
    PARKING_LOT("ParkingLot"),
    PARKING_ZONE("ParkingZone");

    private final String description;

}
