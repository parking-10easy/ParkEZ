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
    CANT_CANCEL_RESERVATION(HttpStatus.BAD_REQUEST, "RESERVATION_003", "결제 대기 중 또는 확인 완료 된 예약만 취소할 수 있습니다."),
    CANT_CANCEL_WITHIN_ONE_HOUR(HttpStatus.BAD_REQUEST, "RESERVATION_004", "예약 시작 시간으로부터 1시간 이내일 경우 예약을 취소할 수 없습니다."),
    CANT_RESERVE_UNAVAILABLE_PARKING_ZONE(HttpStatus.BAD_REQUEST, "RESERVATION_012", "예약 가능한 주차 공간이 아닙니다."),
    CANT_RESERVE_AT_CLOSE_TIME(HttpStatus.BAD_REQUEST, "RESERVATION_013", "영업 시간 외에는 예약할 수 없습니다."),
    // CONFLICT
    ALREADY_RESERVED(HttpStatus.CONFLICT, "RESERVATION_005", "이미 예약이 존재합니다."),
    // NOT_FOUND
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "RESERVATION_006", "예약이 존재하지 않습니다."),
    // UNAUTHORIZED
    NOT_MY_RESERVATION(HttpStatus.UNAUTHORIZED, "RESERVATION_007", "예약한 사용자 본인이 아닙니다."),
    // DistributedLock
    RESERVATION_LOCK_FAILED(HttpStatus.CONFLICT, "RESERVATION_008", "락 획득 실패"),
    RESERVATION_LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "RESERVATION_009", "락 대기 중 인터럽트 발생"),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RESERVATION_UNKNOWN_ERROR", "예약 생성 중 알 수 없는 오류 발생");
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
