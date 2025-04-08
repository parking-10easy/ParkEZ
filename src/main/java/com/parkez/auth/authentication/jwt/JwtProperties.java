package com.parkez.auth.authentication.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
public class JwtProperties {

	private final String secretKey;
	private final long accessTokenExpiration;
	private final long refreshTokenExpiration;

}
