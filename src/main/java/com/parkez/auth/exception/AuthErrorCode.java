package com.parkez.auth.exception;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "TOKEN_NOT_FOUND", "JWT 토큰을 찾을 수 없습니다."),
	INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "INVALID_JWT_SIGNATURE", "유효하지 않는 JWT 서명입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "만료된 JWT 토큰 입니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", "지원하지 않는 JWT 토큰입니다."),
	;
	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
