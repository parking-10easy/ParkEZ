package com.parkez.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "사용자 비밀번호 변경 요청")
public class UserChangePasswordRequest {

	@NotBlank(message = "기존 비밀번호는 필수 입력 항목입니다.")
	@Schema(description = "사용자 기존 비밀번호", example = "1Q2w3e4r!")
	private String oldPassword;

	@NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&]).{8,}$"
		, message = "대문자, 숫자, 특수문자(!,@,#,$,%,^,&)를 최소 1개 이상 포함한 8자리 이상으로 입력해주세요.")
	@Schema(description = "사용자 새 비밀번호", example = "1q2W3e4r!")
	private String newPassword;

	public UserChangePasswordRequest() {
	}

	@Builder
	private UserChangePasswordRequest(String oldPassword, String newPassword) {
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}
}
