package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "소셜 로그인 콜백 요청 DTO")
public class SocialLoginCallbackRequest {

	@NotBlank(message = "인가 코드는 필수입니다.")
	@Schema(description = "인가 코드 (authorization code)", example = "abc123xyz456")
	private final String code;

	@Schema(description = "로그인 시도한 사용자 유형 (user 또는 owner)", example = "user")
	@NotBlank(message = "로그인 유형(state)은 필수입니다.")
	private final String state;

}
