package com.parkez.promotion.excption;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

	// 400 BadRequest
	INVALID_DISCOUNT_PERCENT_VALUE(HttpStatus.BAD_REQUEST, "COUPON_001", "할인율은 20%를 초과할 수 없습니다"),
	INVALID_DISCOUNT_FIXED_VALUE(HttpStatus.BAD_REQUEST,"COUPON_002","고정 할인 금액은 최소 1,000원 이상이어야 합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
