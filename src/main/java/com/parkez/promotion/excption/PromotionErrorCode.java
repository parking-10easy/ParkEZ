package com.parkez.promotion.excption;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionErrorCode implements ErrorCode {

	//400
	INVALID_START_DATE(HttpStatus.BAD_REQUEST, "PROMOTION_001", "프로모션 시작일은 과거일 수 없습니다."),
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "PROMOTION_002", "프로모션 시작일은 종료일보다 이전이어야 합니다.")
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
