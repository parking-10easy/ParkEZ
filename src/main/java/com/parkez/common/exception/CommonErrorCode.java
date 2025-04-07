package com.parkez.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{

	ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_001", "접근 권한이 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다."),
	AUTHORITY_NOT_FOUND(HttpStatus.NOT_FOUND,"COMMON_003" , "권한이 없는 사용자입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;

}
