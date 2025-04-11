package com.parkez.auth.oauth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public class KakaoProperties {
	private final String clientId;
	private final String clientSecret;
	private final String redirectUri;
	private final String authorizationGrantType;
	private final String clientName;
}