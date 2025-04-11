package com.parkez.user.domain.enums;

import java.util.Arrays;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {

	NORMAL("normal","일반"),
	GOOGLE("google","구글"),
	KAKAO("kakao","카카오"),
	NAVER("naver","네이버")
	;

	private final String providerName;
	private final String description;

	public static LoginType of(String providerName) {
		return Arrays.stream(values())
			.filter(loginType -> loginType.providerName.equalsIgnoreCase(providerName))
			.findFirst()
			.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.INVALID_LOGIN_TYPE));
	}

}
