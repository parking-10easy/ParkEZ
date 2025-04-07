package com.parkez.auth.authentication.jwt;

import org.springframework.stereotype.Service;

import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenWriter {

	private final JwtProvider jwtProvider;
	private final RefreshTokenStore refreshTokenStore;

	public TokenResponse createSignupTokenPair(Long userId, String email, String roleName, String nickname) {
		String accessToken = jwtProvider.createAccessToken(userId, email, roleName, nickname);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		refreshTokenStore.save(userId, refreshToken);

		return TokenResponse.of(accessToken, refreshToken);
	}

	public TokenResponse createSigninTokenPair(Long userId, String email, String roleName, String nickname) {
		String accessToken = jwtProvider.createAccessToken(userId, email, roleName, nickname);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		refreshTokenStore.replace(userId, refreshToken);

		return TokenResponse.of(accessToken, refreshToken);
	}

}
