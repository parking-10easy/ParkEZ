package com.parkez.auth.exception;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "JWT 토큰을 찾을 수 없습니다."),
	INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 JWT 서명입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 JWT 토큰입니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원하지 않는 JWT 토큰입니다."),
	DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AUTH_005", "이미 가입된 이메일입니다."),
	;
	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
