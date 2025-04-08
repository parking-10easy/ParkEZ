package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "리프레시 토큰 재발급 요청 DTO")
public class RefreshTokenRequest {

	@NotBlank(message = "리프레시 토큰은 필수 입력입니다.")
	@Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
	private String refreshToken;
}
