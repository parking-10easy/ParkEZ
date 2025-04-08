package com.parkez.reservation.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    // BAD_REQUEST
    NOT_VALID_REQUEST_TIME(HttpStatus.BAD_REQUEST, "RESERVATION_001", "올바르지 않은 예약 시간 요청입니다."),
    CANT_MODIFY_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "RESERVATION_002", "사용 중인 예약이 아닙니다."),
    CANT_CANCEL_COMPLETED_RESERVATION(HttpStatus.BAD_REQUEST, "RESERVATION_003", "사용 완료된 예약은 취소할 수 없습니다."),
    CANT_CANCEL_CANCELED_RESERVATION(HttpStatus.BAD_REQUEST, "RESERVATION_004", "이미 취소된 예약은 취소할 수 없습니다."),
    CANT_CANCEL_WITHIN_ONE_HOUR(HttpStatus.BAD_REQUEST, "RESERVATION_005", "예약 시작 시간으로부터 1시간 이내일 경우 예약을 취소할 수 없습니다."),
    // CONFLICT
    ALREADY_RESERVED(HttpStatus.CONFLICT, "RESERVATION_006", "이미 예약이 존재합니다."),
    // NOT_FOUND
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "RESERVATION_007", "예약이 존재하지 않습니다."),
    // UNAUTHORIZED
    NOT_MY_RESERVATION(HttpStatus.UNAUTHORIZED, "RESERVATION_008", "예약한 사용자 본인이 아닙니다."),

    NOT_FOUND_PARKING_ZONE(HttpStatus.NOT_FOUND, "RESERVATION_009", "주차공간이 존재하지 않습니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "RESERVATION_010", "유저가 존재하지 않습니다."),
    NOT_MY_PARKING_ZONE(HttpStatus.UNAUTHORIZED, "RESERVATION_010", "주차공간 소유주가 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
