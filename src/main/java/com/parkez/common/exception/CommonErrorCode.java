package com.parkez.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode{

	// 401 Unauthorized
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_004", "인증이 필요합니다."),

	// 403 Forbidden
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_001", "접근 권한이 없습니다."),
	MEMBER_INFO_NOT_COMPLETED(HttpStatus.FORBIDDEN, "COMMON_005", "추가정보 입력이 완료되지 않았습니다."),

	// 404 Not Found
	AUTHORITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "권한이 없는 사용자입니다."),

	// 500 Internal Server Error
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;

}
