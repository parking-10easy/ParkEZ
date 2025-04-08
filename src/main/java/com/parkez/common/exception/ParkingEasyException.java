package com.parkez.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ParkingEasyException extends RuntimeException {
    private final ErrorCode errorCode;

    public HttpStatus getStatus() {
        return errorCode.getHttpStatus();
    }

    public ParkingEasyException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
}
