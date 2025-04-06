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
	USER_ALREADY_DELETED(HttpStatus.FORBIDDEN,"USER_003","탈퇴한 사용자입니다." ),
	BUSINESS_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "USER_004", "사업자 정보는 모두 입력해야 합니다."),
	USER_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST,"USER_005","새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String defaultMessage;


}
