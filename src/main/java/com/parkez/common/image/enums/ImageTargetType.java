package com.parkez.common.image.enums;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.image.exception.ImageErrorCode;
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

    public static ImageTargetType of(String type){
        return Arrays.stream(values())
                .filter(e->e.description.equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(()->new ParkingEasyException(ImageErrorCode.INVALID_TARGET_TYPE));
    }
}
