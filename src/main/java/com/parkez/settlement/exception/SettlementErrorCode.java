package com.parkez.settlement.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    // 404 Not Found
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT_001", "조회된 정산 데이터가 없습니다."),

    // 400 Bad Request
    INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "SETTLEMENT_002", "유효하지 않은 연월입니다."),
    NOT_SETTLEMENT_ELIGIBLE(HttpStatus.BAD_REQUEST, "SETTLEMENT_003", "해당 예약 건은 정산 대상이 아닙니다."),
    ALREADY_SETTLED(HttpStatus.BAD_REQUEST, "SETTLEMENT_004", "해당 월은 이미 정산 완료되었습니다."),
    SETTLEMENT_NOT_CONFIRMABLE(HttpStatus.BAD_REQUEST, "SETTLEMENT_005", "정산 확정 상태일 때만 완료 처리가 가능합니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
