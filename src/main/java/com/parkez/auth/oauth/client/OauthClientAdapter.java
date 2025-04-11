package com.parkez.auth.oauth.client;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OauthClientAdapter {

	private final List<OAuthClient> OAuthClients;

	public OAuthClient getClient(String providerName) {
		return OAuthClients.stream()
			.filter(client -> client.supports(providerName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 로그인입니다."));
	}

}
