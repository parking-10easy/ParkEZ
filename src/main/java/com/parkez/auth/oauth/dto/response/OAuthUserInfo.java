package com.parkez.auth.oauth.dto.response;

import com.parkez.auth.oauth.enums.OAuthProvider;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthUserInfo {
	private final Long id;
	private final String email;
	private final String nickname;
	private final OAuthProvider provider;

	@Builder
	private OAuthUserInfo(Long id, String email, String nickname, OAuthProvider provider) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
	}

	public static OAuthUserInfo of(KakaoUserInfoResponse kakaoUserInfoResponse, OAuthProvider provider) {
		return OAuthUserInfo.builder()
			.id(kakaoUserInfoResponse.getId())
			.email(kakaoUserInfoResponse.getEmail())
			.nickname(kakaoUserInfoResponse.getNickname())
			.provider(provider)
			.build();
	}

}
