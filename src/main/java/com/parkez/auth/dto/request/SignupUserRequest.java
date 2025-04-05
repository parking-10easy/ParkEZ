package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "사용자 회원가입 요청")
public class SignupUserRequest {

	@NotBlank
	@Email
	@Schema(description = "사용자 이메일", example = "user@example.com")
	private String email;
	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&]).{8,}$"
		, message = "대문자, 숫자, 특수문자(!,@,#,$,%,^,&)를 최소 1개 이상 포함한 8자리 이상으로 입력해주세요.")
	@Schema(description = "비밀번호", example = "1Q2w3e4r!")
	private String password;

	@NotBlank
	@Schema(description = "사용자 닉네임", example = "user")
	private String nickname;
	@NotBlank
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	public SignupUserRequest() {
	}

	@Builder
	private SignupUserRequest(String email, String password, String nickname, String phone) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.phone = phone;
	}
}
