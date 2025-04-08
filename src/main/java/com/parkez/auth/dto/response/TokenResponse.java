package com.parkez.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "토큰 응답 DTO")
public class TokenResponse {

	@Schema(description = "발급된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private final String accessToken;

	@Schema(description = "발급된 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private final String refreshToken;

	private TokenResponse(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public static TokenResponse of(String accessToken, String refreshToken) {
		return new TokenResponse(accessToken, refreshToken);
	}
}
