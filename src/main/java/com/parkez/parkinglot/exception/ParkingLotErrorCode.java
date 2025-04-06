package com.parkez.parkinglot.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ParkingLotErrorCode implements ErrorCode {
    NOT_OWNER("NOT_OWNER", HttpStatus.FORBIDDEN, "소유자 권한이 아닙니다."),
    NOT_FOUND("NOT_FOUND", HttpStatus.FORBIDDEN, "주차장을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ParkingLotErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

}
