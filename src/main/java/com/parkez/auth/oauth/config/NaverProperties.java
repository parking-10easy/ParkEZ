package com.parkez.auth.oauth.config;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.naver")
public class NaverProperties {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String authorizationGrantType;
    private final String clientAuthenticationMethod;
    private final String clientName;
    private final String[] scope;
}


