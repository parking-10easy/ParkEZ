package com.parkez.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "회원가입 응답 DTO")
public class SignupResponse {

	@Schema(description = "유저 ID", example = "1")
	private final Long id;

	@Schema(description = "유저 이메일", example = "user@example.com")
	private final String email;

	@Schema(description = "발급된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private final String accessToken;

	@Schema(description = "발급된 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
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
