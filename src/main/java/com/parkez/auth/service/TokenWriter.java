package com.parkez.auth.service;

import org.springframework.stereotype.Service;

import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.jwt.JwtProvider;
import com.parkez.auth.refresh.RefreshTokenStore;
import com.parkez.user.domain.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenWriter {

	private final JwtProvider jwtProvider;
	private final RefreshTokenStore refreshTokenStore;

	public TokenResponse createSignupTokenPair(Long userId, String email, UserRole role, String nickname) {
		String accessToken = jwtProvider.createAccessToken(userId, email, role, nickname);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		refreshTokenStore.save(userId, refreshToken);

		return TokenResponse.of(accessToken, refreshToken);
	}

}
