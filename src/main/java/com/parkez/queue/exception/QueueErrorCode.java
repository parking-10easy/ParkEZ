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
    DTO(HttpStatus.CONFLICT,"QUEUE_002", "dto 변환 중 오류"),
    DTO2(HttpStatus.CONFLICT,"QUEUE_002", "dto 변환 중 오류2"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}