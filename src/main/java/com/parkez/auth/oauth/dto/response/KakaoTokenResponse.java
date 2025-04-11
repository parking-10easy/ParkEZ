package com.parkez.auth.oauth.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoTokenResponse {

	private final String tokenType;
	private final String accessToken;
	private final String idToken;
	private final long expiresIn;
	private final String refreshToken;
	private final String refreshTokenExpiresIn;
	private final String scope;

	public KakaoTokenResponse(String tokenType, String accessToken, String idToken, long expiresIn, String refreshToken,
		String refreshTokenExpiresIn, String scope) {
		this.tokenType = tokenType;
		this.accessToken = accessToken;
		this.idToken = idToken;
		this.expiresIn = expiresIn;
		this.refreshToken = refreshToken;
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
		this.scope = scope;
	}
}
