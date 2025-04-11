package com.parkez.auth.authentication.refresh;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.parkez.auth.authentication.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

	private final StringRedisTemplate redisTemplate;
	private final JwtProperties jwtProperties;
	private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";

	public void set(Long userId, String refreshTokenValue) {
		String key = getKey(userId);
		redisTemplate.opsForValue().set(key, refreshTokenValue, Duration.ofSeconds(jwtProperties.getRefreshTokenExpiration()));
	}

	public boolean existsBy(Long userId) {
		return redisTemplate.hasKey(getKey(userId));
	}

	public String get(Long userId) {
		String key = getKey(userId);
		return redisTemplate.opsForValue().get(key);
	}

	private String getKey(Long userId) {
		return REFRESH_TOKEN_KEY_PREFIX + userId;
	}



}
