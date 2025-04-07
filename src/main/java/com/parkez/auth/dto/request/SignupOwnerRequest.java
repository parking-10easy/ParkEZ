package com.parkez.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "오너 회원가입 요청")
public class SignupOwnerRequest {

	@NotBlank
	@Email
	@Schema(description = "이메일", example = "owner@example.com")
	private String email;
	@NotBlank
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&]).{8,}$"
		, message = "대문자, 숫자, 특수문자(!,@,#,$,%,^,&)를 최소 1개 이상 포함한 8자리 이상으로 입력해주세요.")
	@Schema(description = "비밀번호", example = "1Q2w3e4r!")
	private String password;

	@NotBlank
	@Schema(description = "닉네임", example = "user")
	private String nickname;

	@NotBlank
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	@NotBlank
	@Schema(description = "사업자등록번호", example = "123-45-67890")
	@Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 양식에 맞지 않습니다. ex) 123-45-67890")
	private String businessNumber;

	@NotBlank
	@Schema(description = "은행명", example = "신한은행")
	private String bankName;

	@NotBlank
	@Schema(description = "계좌번호", example = "110-1234-5678")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "계좌번호 양식에 맞지 않습니다. ex) 110-1234-5678")
	private String bankAccount;

	@NotBlank
	@Schema(description = "예금주명", example = "홍길동")
	private String depositorName;

	public SignupOwnerRequest() {
	}

	@Builder
	private SignupOwnerRequest(String email, String password, String nickname, String phone, String businessNumber,
		String bankName, String bankAccount, String depositorName) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.phone = phone;
		this.businessNumber = businessNumber;
		this.bankName = bankName;
		this.bankAccount = bankAccount;
		this.depositorName = depositorName;
	}
}
