package com.parkez.auth.oauth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KakaoProperties.class, NaverProperties.class, NaverProviderProperties.class})
public class OAuthConfig {
}