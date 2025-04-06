package com.parkez.reservation.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    NOT_VALID_REQUEST_TIME(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "올바르지 않은 예약 시간 요청입니다."),
    ALREADY_RESERVED(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "이미 예약이 존재합니다."),
    NOT_FOUND_RESERVATION(String.valueOf(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND, "예약이 존재하지 않습니다."),
    NOT_MY_RESERVATION(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "예약한 사용자 본인이 아닙니다."),

    NOT_FOUND_PARKING_ZONE(String.valueOf(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND, "주차공간이 존재하지 않습니다."),
    NOT_MY_PARKING_ZONE(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "주차공간 소유주가 아닙니다.")
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
