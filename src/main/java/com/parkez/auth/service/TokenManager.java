package com.parkez.auth.service;

import org.springframework.stereotype.Service;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenManager {

	private final JwtProvider jwtProvider;
	private final RefreshTokenStore refreshTokenStore;

	public TokenResponse issueTokens(User user) {
		String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(),
			user.getRoleName(), user.getNickname(), user.isSignupCompleted());

		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		refreshTokenStore.set(user.getId(),refreshToken);

		return TokenResponse.of(accessToken, refreshToken);
	}

	public Long extractUserId(String refreshToken) {
		return jwtProvider.extractUserId(refreshToken);
	}

	public void validateRefreshTokenExists(Long userId) {
		if (!refreshTokenStore.existsBy(userId)) {
			throw new ParkingEasyException(AuthErrorCode.TOKEN_NOT_FOUND);
		}
	}

	public String reissueAccessToken(User user) {

		return jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleName(),
			user.getNickname(), user.isSignupCompleted());
	}
}
