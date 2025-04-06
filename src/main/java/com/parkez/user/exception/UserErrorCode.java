package com.parkez.user.exception;

import org.springframework.http.HttpStatus;

import com.parkez.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_001", "존재하지 않는 이메일 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER_002","유저를 찾을 수 없습니다." ),
	USER_ALREADY_DELETED(HttpStatus.FORBIDDEN,"USER_003","탈퇴한 사용자입니다." );
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;


}
