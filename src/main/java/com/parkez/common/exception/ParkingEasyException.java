package com.parkez.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ParkingEasyException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    public ParkingEasyException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
    }

    public ParkingEasyException(ErrorCode errorCode, String message) {
        super(message);
        this.status = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
        this.message = message;
    }
}
