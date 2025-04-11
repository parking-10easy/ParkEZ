package com.parkez.parkinglot.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingLotErrorCode implements ErrorCode {

    // UNAUTHORIZED
    NOT_PARKING_LOT_OWNER(HttpStatus.UNAUTHORIZED, "PARKING_001", "주차장 소유자가 아닙니다."),

    // NOT_FOUND
    NOT_FOUND(HttpStatus.NOT_FOUND, "PARKING_002", "주차장을 찾을 수 없습니다."),

    // BAD_REQUEST
    INVALID_PARKING_LOT_STATUS(HttpStatus.BAD_REQUEST, "PARKING_004", "올바르지 않은 주차 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
