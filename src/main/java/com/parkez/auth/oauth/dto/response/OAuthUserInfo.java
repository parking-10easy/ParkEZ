package com.parkez.auth.oauth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthUserInfo {
	private final Long id;
	private final String email;
	private final String nickname;
	private final String providerName;

	@Builder
	private OAuthUserInfo(Long id, String email, String nickname, String providerName) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.providerName = providerName;
	}

	public static OAuthUserInfo of(KakaoUserInfoResponse kakaoUserInfoResponse, String providerName) {
		return OAuthUserInfo.builder()
			.id(kakaoUserInfoResponse.getId())
			.email(kakaoUserInfoResponse.getEmail())
			.nickname(kakaoUserInfoResponse.getNickname())
			.providerName(providerName)
			.build();
	}

}
