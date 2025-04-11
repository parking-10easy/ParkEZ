package com.parkez.auth.service;

import org.springframework.stereotype.Service;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenRedisStore;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenManager {

	private final JwtProvider jwtProvider;
	private final RefreshTokenRedisStore refreshTokenRedisStore;

	public TokenResponse issueTokens(User user) {
		String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(),
			user.getRoleName(), user.getNickname(), user.isSignupCompleted());

		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		refreshTokenRedisStore.set(user.getId(),refreshToken);

		return TokenResponse.of(accessToken, refreshToken);
	}

	public Long extractUserId(String refreshToken) {
		return jwtProvider.extractUserId(refreshToken);
	}

	public void validateRefreshToken(Long userId) {
		if (!refreshTokenRedisStore.existsBy(userId)) {
			throw new ParkingEasyException(AuthErrorCode.TOKEN_NOT_FOUND);
		}
	}

	public String reissueAccessToken(User user) {

		return jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleName(),
			user.getNickname(), user.isSignupCompleted());
	}
}
