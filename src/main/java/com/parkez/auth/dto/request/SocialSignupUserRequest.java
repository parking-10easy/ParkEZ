package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "소셜 사용자 회원가입 요청")
public class SocialSignupUserRequest {

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 주소 형식이어야 합니다.")
	@Schema(description = "사용자 이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	@Schema(description = "사용자 닉네임", example = "user")
	private String nickname;

	@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	public SocialSignupUserRequest() {
	}

	@Builder
	private SocialSignupUserRequest(String email, String nickname, String phone) {
		this.email = email;
		this.nickname = nickname;
		this.phone = phone;
	}
}
