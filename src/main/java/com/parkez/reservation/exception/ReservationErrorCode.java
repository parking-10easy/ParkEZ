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
    CANT_MODIFY_RESERVATION_STATUS(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "사용 중인 예약이 아닙니다."),
    CANT_CANCEL_COMPLETED_RESERVATION(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "사용 완료된 예약은 취소할 수 없습니다."),
    CANT_CANCEL_CANCELED_RESERVATION(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "이미 취소된 예약은 취소할 수 없습니다."),
    CANT_CANCEL_WITHIN_ONE_HOUR(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "예약 시작 시간으로부터 1시간 이내일 경우 예약을 취소할 수 없습니다."),

    NOT_FOUND_PARKING_ZONE(String.valueOf(HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND, "주차공간이 존재하지 않습니다."),
    NOT_MY_PARKING_ZONE(String.valueOf(HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST, "주차공간 소유주가 아닙니다.")
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
