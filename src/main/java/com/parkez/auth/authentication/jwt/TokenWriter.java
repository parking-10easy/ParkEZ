package com.parkez.auth.authentication.jwt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.authentication.refresh.RefreshToken;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenWriter {

	private final JwtProvider jwtProvider;
	private final UserReader userReader;
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

	public TokenResponse reissueToken(String refreshToken) {

		RefreshToken savedRefreshToken = refreshTokenStore.findByToken(refreshToken).orElseThrow(() -> new ParkingEasyException(
			AuthErrorCode.TOKEN_NOT_FOUND));

		if (jwtProvider.isTokenExpired(refreshToken)) {
			throw new ParkingEasyException(AuthErrorCode.TOKEN_EXPIRED);
		}

		Claims claims = jwtProvider.extractClaims(refreshToken);
		Long userId = Long.valueOf(claims.getSubject());

		User user = userReader.getActiveById(userId);

		String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleName(), user.getNickname());

		return TokenResponse.of(newAccessToken, refreshToken);
	}

}
