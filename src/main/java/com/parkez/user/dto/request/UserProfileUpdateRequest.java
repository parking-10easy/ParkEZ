package com.parkez.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "사용자 프로필 수정 요청")
public class UserProfileUpdateRequest {

	@NotBlank
	@Schema(description = "사용자 닉네임", example = "updated user")
	private String nickname;

	@NotBlank
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식에 맞지 않습니다. ex) 010-1234-5678")
	@Schema(description = "전화번호", example = "010-2345-6789")
	private String phone;

	@Schema(description = "사업자등록번호", example = "123-45-67890", nullable = true)
	@Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 양식에 맞지 않습니다. ex) 123-45-67890")
	private String businessNumber;

	@Schema(description = "은행명", example = "신한은행", nullable = true)
	private String bankName;
	@Schema(description = "계좌번호", example = "110-1234-5678", nullable = true)
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "계좌번호 양식에 맞지 않습니다. ex) 110-1234-5678")
	private String bankAccount;
	@Schema(description = "예금주명", example = "홍길동", nullable = true)
	private String depositorName;

	public UserProfileUpdateRequest() {
	}

	@Builder
	private UserProfileUpdateRequest(String nickname, String phone, String businessNumber, String bankName,
		String bankAccount, String depositorName) {
		this.nickname = nickname;
		this.phone = phone;
		this.businessNumber = businessNumber;
		this.bankName = bankName;
		this.bankAccount = bankAccount;
		this.depositorName = depositorName;
	}
}
