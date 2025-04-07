package com.parkez.auth.service;

import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkez.auth.authentication.jwt.TokenWriter;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.user.domain.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class TokenWriterTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenStore refreshTokenStore;

	@InjectMocks
	private TokenWriter tokenWriter;

	@Test
	public void 토큰_생성_성공() {
		//given
		String accessToken = "mockAccess";
		String refreshToken = "mockRefresh";
		given(jwtProvider.createAccessToken(anyLong(), anyString(), any(String.class), anyString())).willReturn(
			accessToken);
		given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

		//when
		TokenResponse tokenResponse = tokenWriter.createSignupTokenPair(1L, "user@example.com", UserRole.ROLE_USER, "테스트");
		//then
		Assertions.assertThat(tokenResponse)
			.extracting(
				"accessToken",
				"refreshToken"
			).containsExactly(
				accessToken,
				refreshToken
			);

	}
}