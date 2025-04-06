package com.parkez.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyProfileResponse {

	private final long id;
	private final String email;
	private final String nickname;
	private final String phone;
	private final String profileImageUrl;
	private final String businessNumber;
	private final String bankName;
	private final String bankAccount;
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
		BusinessAccountInfo businessAccountInfo = user.getBusinessAccountInfo();
		return MyProfileResponse.builder()
			.id(user.getId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.phone(user.getPhone())
			.profileImageUrl(user.getProfileImageUrl())
			.businessNumber(businessAccountInfo != null ? businessAccountInfo.getBusinessNumber() : null)
			.bankName(businessAccountInfo != null ? businessAccountInfo.getBankName() : null)
			.bankAccount(businessAccountInfo != null ? businessAccountInfo.getBankAccount() : null)
			.depositorName(businessAccountInfo != null ? businessAccountInfo.getDepositorName() : null)
			.build();
	}
}
