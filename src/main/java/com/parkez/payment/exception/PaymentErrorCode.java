package com.parkez.payment.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    // 404 Not Found
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "예약을 찾을 수 없습니다."),

    // 400 Bad Request
    ALREADY_PAID_RESERVATION(HttpStatus.BAD_REQUEST, "PAYMENT_002", "이미 결제 승인/취소된 예약입니다. 새로운 예약을 생성해주세요."),
    PAYMENT_TIME_OUT(HttpStatus.BAD_REQUEST, "PAYMENT_003", "예약 후 30분이 경과되어 결제가 불가능합니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
