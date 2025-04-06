package com.parkez.parkingzone.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneErrorCode implements ErrorCode {

    PARKING_ZONE_NOT_FOUND("404", HttpStatus.NOT_FOUND, "해당 주차공간을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
