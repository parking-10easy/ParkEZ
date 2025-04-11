package com.parkez.auth.oauth.dto.response;

import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoUserInfoResponse {

	private final Long id;
	private final KakaoAccount kakaoAccount;

	@Getter
	@RequiredArgsConstructor
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	static class KakaoAccount {
		private final String email;
		private final Profile profile;

		@Getter
		@RequiredArgsConstructor
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		static class Profile {
			private final String nickname;
		}

	}

	public String getNickname() {
		return Optional.ofNullable(this.kakaoAccount)
			.map(KakaoAccount::getProfile)
			.map(KakaoAccount.Profile::getNickname)
			.orElse(null);
	}

	public String getEmail() {
		return Optional.ofNullable(this.kakaoAccount)
			.map(KakaoAccount::getEmail)
			.orElse(null);
	}

	public Long getId() {
		return this.id;
	}
}
