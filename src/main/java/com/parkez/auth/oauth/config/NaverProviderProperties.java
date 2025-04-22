package com.parkez.auth.oauth.config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.naver")
public class NaverProviderProperties {
    private final String authorizationUri;
    private final String tokenUri;
    private final String userInfoUri;
    private final String userNameAttribute;
}
