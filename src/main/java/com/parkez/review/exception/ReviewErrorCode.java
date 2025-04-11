package com.parkez.review.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "REVIEW_001", "이미 작성한 리뷰입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_002", "해당 리뷰를 찾을 수 없습니다."),
    NOT_REVIEW_OWNER(HttpStatus.NOT_FOUND, "REVIEW_003", "리뷰의 작성자 본인이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
