package com.parkez.auth.oauth.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

	KAKAO("kakao"),
	GOOGLE("google"),
	NAVER("naver");

	private final String description;

	public static OAuthProvider from(String providerName) {
		return Arrays.stream(OAuthProvider.values())
			.filter(oAuthProvider -> oAuthProvider.description.equalsIgnoreCase(providerName))
			.findFirst()
			.orElseThrow(()->new IllegalArgumentException("Unknown OAuth Provider: " + providerName));
	}

}
