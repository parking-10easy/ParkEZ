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

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 주소 형식이어야 합니다.")
	@Schema(description = "이메일", example = "owner@example.com")
	private String email;

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&]).{8,}$"
		, message = "대문자, 숫자, 특수문자(!,@,#,$,%,^,&)를 최소 1개 이상 포함한 8자리 이상으로 입력해주세요.")
	@Schema(description = "비밀번호", example = "1Q2w3e4r!")
	private String password;

	@NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	@Schema(description = "닉네임", example = "user")
	private String nickname;

	@NotBlank(message = "전화번호는 필수 입력 항목입니다.")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phone;

	@NotBlank(message = "사업자등록번호는 필수 입력 항목입니다.")
	@Schema(description = "사업자등록번호", example = "123-45-67890")
	@Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 양식에 맞지 않습니다. ex) 123-45-67890")
	private String businessNumber;

	@NotBlank(message = "은행명 필수 입력 항목입니다.")
	@Schema(description = "은행명", example = "신한은행")
	private String bankName;

	@NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
	@Schema(description = "계좌번호", example = "110-1234-5678")
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "계좌번호 양식에 맞지 않습니다. ex) 110-1234-5678")
	private String bankAccount;

	@NotBlank(message = "예금주명은 필수 입력 항목입니다.")
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
