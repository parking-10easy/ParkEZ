package com.parkez.queue.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QueueErrorCode implements ErrorCode {

    JOINED_WAITING_QUEUE(HttpStatus.OK, "QUEUE_001", "해당 시간에 이미 예약이 존재하여 대기열에 저장되었습니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT,"QUEUE_002", "이미 대기열에 등록된 사용자입니다."),
    NOT_IN_QUEUE(HttpStatus.BAD_REQUEST, "QUEUE_003", "대기열에 존재하지 않습니다"),
    UNKNOWN_QUEUE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "QUEUE_004", "알수없는 에러 발생"),

    DTO_CONVERT_FAIL_NUMBER(HttpStatus.CONFLICT, "QUEUE_005", "DTO 변환 실패 - 숫자 타입 변환 오류"),
    DTO_CONVERT_FAIL_TIME(HttpStatus.CONFLICT, "QUEUE_006", "DTO 변환 실패 - 시간 변환 오류"),
    DTO_CONVERT_FAIL_NULL(HttpStatus.BAD_REQUEST, "QUEUE_007", "DTO 변환 실패 - 필수 필드 누락"),
    DTO_CONVERT_FAIL_TYPE(HttpStatus.CONFLICT, "QUEUE_008", "DTO 변환 실패 - 객체 타입 오류"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}