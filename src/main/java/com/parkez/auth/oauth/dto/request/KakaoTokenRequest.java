package com.parkez.auth.oauth.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KakaoTokenRequest {

	private final String grantType;
	private final String clientId;
	private final String clientSecret;
	private final String redirectUri;
	private final String code;

	@Builder
	private KakaoTokenRequest(String grantType, String clientId, String clientSecret, String redirectUri, String code) {
		this.grantType = grantType;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		this.code = code;
	}
}
