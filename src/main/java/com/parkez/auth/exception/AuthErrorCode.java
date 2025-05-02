package com.parkez.auth.exception;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	// 400 BadRequest
	INVALID_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "AUTH_008","지원하지 않는 로그인 타입입니다"),
	INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST,"AUTH_010", "지원하지 않는 소셜 로그인입니다."),

	// 401 Unauthorized
	INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 JWT 서명입니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 JWT 토큰입니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원하지 않는 JWT 토큰입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 비밀번호 입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_007", "로그인이 필요합니다."),

	// 404 Not Found
	TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "JWT 토큰을 찾을 수 없습니다."),

	// 409 Conflict
	DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AUTH_005", "이미 가입된 이메일입니다."),
	ALREADY_COMPLETED(HttpStatus.CONFLICT,"AUTH_009","이미 추가정보 입력이 완료된 사용자입니다."),

	// 502 Bad Gateway
	OAUTH_ACCESS_TOKEN_FAILED(HttpStatus.BAD_GATEWAY,"AUTH_011","OAuth Access Token 발급 실패"),
	OAUTH_USERINFO_FAILED(HttpStatus.BAD_GATEWAY,"AUTH_012","OAuth 사용자 정보 요청 실패");




	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;
}
