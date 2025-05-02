package com.parkez.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SocialSignupCompleteResponse {

	private final String accessToken;

	private SocialSignupCompleteResponse(String accessToken) {
		this.accessToken = accessToken;
	}

	public static SocialSignupCompleteResponse from(String accessToken) {
		return new SocialSignupCompleteResponse(accessToken);
	}
}
