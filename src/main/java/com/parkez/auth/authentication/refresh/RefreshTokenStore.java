package com.parkez.auth.authentication.refresh;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.parkez.auth.authentication.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	public void delete(Long userId) {
		String key = getKey(userId);
		Boolean result = redisTemplate.delete(key);
		log.info("회원탈퇴 처리 중 리프레시 토큰 {}됨 (key={})", Boolean.TRUE.equals(result) ? "삭제" : "존재하지 않아 생략", key);
	}

	private String getKey(Long userId) {
		return REFRESH_TOKEN_KEY_PREFIX + userId;
	}


}
