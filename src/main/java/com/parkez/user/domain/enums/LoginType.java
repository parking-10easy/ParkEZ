package com.parkez.user.domain.enums;

import java.util.Arrays;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {

	NORMAL,
	GOOGLE,
	KAKAO,
	NAVER
	;

	public static LoginType from(String providerName) {
		return Arrays.stream(values())
			.filter(loginType -> loginType.name().equalsIgnoreCase(providerName))
			.findFirst()
			.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.INVALID_LOGIN_TYPE));
	}

}
