package com.parkez.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponse {

	private final Long id;
	private final String email;
	private final String accessToken;
	private final String refreshToken;

	@Builder
	private SignupResponse(Long id, String email, String accessToken, String refreshToken) {
		this.id = id;
		this.email = email;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public static SignupResponse of(Long id, String email, TokenResponse tokenResponse) {
		return SignupResponse.builder()
			.id(id)
			.email(email)
			.accessToken(tokenResponse.getAccessToken())
			.refreshToken(tokenResponse.getRefreshToken())
			.build();
	}
}
