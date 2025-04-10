package com.parkez.review.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "REVIEW_001", "이미 작성한 리뷰입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
