package com.parkez.parkinglot.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingLotErrorCode implements ErrorCode {
    NOT_OWNER(HttpStatus.UNAUTHORIZED, "PARKING_001", "소유자 권한이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
