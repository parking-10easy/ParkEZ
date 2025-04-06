package com.parkez.parkingzone.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneErrorCode implements ErrorCode {

    PARKING_ZONE_NOT_FOUND("404", HttpStatus.NOT_FOUND, "해당 주차공간을 찾을 수 없습니다."),
    FORBIDDEN_TO_UPDATE("403", HttpStatus.FORBIDDEN, "소유자만 수정할 수 있습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
