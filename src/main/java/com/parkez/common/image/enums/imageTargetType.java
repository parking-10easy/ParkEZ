package com.parkez.common.image.enums;

import com.parkez.common.exception.ErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.image.exception.ImageErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum imageTargetType {

    USER_PROFILE("UserProfile"),
    PARKING_LOT("ParkingLot"),
    PARKING_ZONE("ParkingZone");

    private final String description;

    public static imageTargetType fromString(String type){
        return Arrays.stream(values())
                .filter(e->e.description.equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(()->new ParkingEasyException(ImageErrorCode.INVALID_IMAGE_TYPE));
    }
}
