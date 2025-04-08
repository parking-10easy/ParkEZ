package com.parkez.parkinglot.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingLotErrorCode implements ErrorCode {
    NOT_OWNER(HttpStatus.UNAUTHORIZED, "PARKING_001", "소유자 권한이 아닙니다."),
    NOT_PARKING_LOT_OWNER(HttpStatus.UNAUTHORIZED, "PARKING_002", "주차장 소유자가 아닙니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "PARKING_003", "주차장을 찾을 수 없습니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "PARKING_004", "사용자 값이 없습니다."); // 삭제 예정

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
