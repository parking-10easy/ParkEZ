package com.parkez.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "내 프로필 조회 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyProfileResponse {

	@Schema(description = "유저 ID", example = "1")
	private final long id;

	@Schema(description = "유저 이메일", example = "user@example.com")
	private final String email;

	@Schema(description = "유저 닉네임", example = "홍길동")
	private final String nickname;

	@Schema(description = "유저 전화번호", example = "010-1234-5678")
	private final String phone;

	@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
	private final String profileImageUrl;

	@Schema(description = "사업자 등록번호", example = "123-45-67890")
	private final String businessNumber;

	@Schema(description = "은행명", example = "국민은행")
	private final String bankName;

	@Schema(description = "은행 계좌번호", example = "123456-78-901234")
	private final String bankAccount;

	@Schema(description = "예금주 이름", example = "홍길동")
	private final String depositorName;

	@Builder
	private MyProfileResponse(long id, String email, String nickname, String phone, String profileImageUrl,
		String businessNumber, String bankName, String bankAccount, String depositorName) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.phone = phone;
		this.profileImageUrl = profileImageUrl;
		this.businessNumber = businessNumber;
		this.bankName = bankName;
		this.bankAccount = bankAccount;
		this.depositorName = depositorName;
	}

	public static MyProfileResponse from(User user) {
		return MyProfileResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.phone(user.getPhone())
			.profileImageUrl(user.getProfileImageUrl())
			.businessNumber(user.getBusinessNumber())
			.bankName(user.getBankName())
			.bankAccount(user.getBankAccount())
			.depositorName(user.getDepositorName())
			.build();
	}
}
