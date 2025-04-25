package com.parkez.promotion.excption;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionIssueErrorCode implements ErrorCode {

	//400
	EXPIRED_COUPON(HttpStatus.BAD_REQUEST, "PROMOTION_ISSUE_003", "만료된 쿠폰 입니다."),
	INVALID_ISSUE_STATUS(HttpStatus.BAD_REQUEST,"PROMOTION_ISSUE_004","요청한 프로모션 발급 상태 값이 유효하지 않습니다."),
	INVALID_SORT_BY(HttpStatus.BAD_REQUEST,"PROMOTION_ISSUE_005","요청한 프로모션 발급 정렬 값이 유효하지 않습니다."),

	//404
	PROMOTION_ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND,"PROMOTION_ISSUE_001","프로모션 쿠폰을 찾을 수 없습니다."),

	//409
	ALREADY_USED(HttpStatus.CONFLICT,"PROMOTION_ISSUE_002","이미 사용한 프로모션 쿠폰 입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
