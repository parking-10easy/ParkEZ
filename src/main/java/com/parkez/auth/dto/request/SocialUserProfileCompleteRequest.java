package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "일반사용자 소셜 회원가입 추가입력 요청")
public class SocialUserProfileCompleteRequest {

	@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	public SocialUserProfileCompleteRequest() {
	}

	@Builder
	private SocialUserProfileCompleteRequest(String phone) {
		this.phone = phone;
	}
}
