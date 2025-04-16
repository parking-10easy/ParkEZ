package com.parkez.payment.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 404 Not Found
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "해당 결제를 찾을 수 없습니다."),

    // 400 Bad Request
    PAYMENT_TIME_OUT(HttpStatus.BAD_REQUEST, "PAYMENT_002", "예약 후 10분이 경과되어 결제가 불가능합니다."),
    PAYMENT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "PAYMENT_003", "해당 예약 건은 이미 진행 중인 결제가 존재합니다."),
    PAYMENT_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "PAYMENT_004", "이미 결제 완료된 예약입니다."),
    PAYMENT_CANCELED(HttpStatus.BAD_REQUEST, "PAYMENT_005", "결제 실패된 예약입니다. 새로운 예약을 생성해주세요."),
    ILLEGAL_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, "PAYMENT_006", "유효하지 않은 결제 타입입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
