package com.parkez.auth.oauth.enums;

import java.util.Arrays;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

	KAKAO,
	GOOGLE,
	NAVER;

	public static OAuthProvider from(String providerName) {
		return Arrays.stream(OAuthProvider.values())
			.filter(oAuthProvider -> oAuthProvider.name().equalsIgnoreCase(providerName))
			.findFirst()
			.orElseThrow(()->new ParkingEasyException(AuthErrorCode.INVALID_OAUTH_PROVIDER));
	}

}
