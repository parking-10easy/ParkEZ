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
	INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "PROMOTION_002", "프로모션 시작일은 종료일보다 이전이어야 합니다."),
	ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "PROMOTION_004", "발급 가능한 수량을 초과했습니다."),
	QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "PROMOTION_005", "쿠폰이 모두 소진되었습니다."),
	INVALID_ISSUE_STATUS(HttpStatus.BAD_REQUEST,"PROMOTION_006","요청한 프로모션 발급 상태 값이 유효하지 않습니다."),
	INVALID_SORT_BY(HttpStatus.BAD_REQUEST,"PROMOTION_007","요청한 프로모션 발급 정렬 값이 유효하지 않습니다."),

	//404
	PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND,"PROMOTION_003","프로모션을 찾을 수 없습니다.")
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
