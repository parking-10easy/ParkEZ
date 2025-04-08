package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "로그인 요청")
public class SigninRequest {

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 주소 형식이어야 합니다.")
	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	@Schema(description = "비밀번호", example = "1Q2w3e4r!")
	private String password;
}
