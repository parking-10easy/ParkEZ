package com.parkez.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.authentication.jwt.TokenWriter;
import com.parkez.auth.authentication.refresh.RefreshToken;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class TokenWriterTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private UserReader userReader;

	@Mock
	private RefreshTokenStore refreshTokenStore;

	@InjectMocks
	private TokenWriter tokenWriter;

	@Test
	public void 토큰_생성_성공() {
		//given
		String accessToken = "mockAccess";
		String refreshToken = "mockRefresh";
		given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
			accessToken);
		given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

		//when
		TokenResponse tokenResponse = tokenWriter.createSignupTokenPair(1L, "user@example.com", UserRole.ROLE_USER.name(), "테스트");
		//then
		assertThat(tokenResponse)
			.extracting(
				"accessToken",
				"refreshToken"
			).containsExactly(
				accessToken,
				refreshToken
			);

	}

	@Nested
	class ReissueToken {

		@Test
		void 리프레시토큰_만료_예외() {
			// given
			String expiredRefreshToken = "expired-refresh-token";

			RefreshToken savedRefreshToken = RefreshToken.create(1L, expiredRefreshToken);

			given(refreshTokenStore.findByToken(anyString())).willReturn(Optional.of(savedRefreshToken));
			given(jwtProvider.isTokenExpired(anyString())).willReturn(true); // 여기서 만료된 것으로 설정

			// when & then
			assertThatThrownBy(() -> tokenWriter.reissueToken(expiredRefreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.TOKEN_EXPIRED.getDefaultMessage());
		}

		@Test
		void 리프레시토큰_정상_재발급() {
			// given
			String refreshToken = "refresh-token";
			String newAccessToken = "new-access-token";
			Long userId = 1L;

			User user = User.builder()
				.email("test@example.com")
				.role(UserRole.ROLE_USER)
				.nickname("nickname")
				.build();
			ReflectionTestUtils.setField(user,"id", 1L);
			RefreshToken savedRefreshToken = RefreshToken.create(userId,refreshToken);

			Claims claims = Jwts.claims().subject(String.valueOf(userId)).build();

			given(refreshTokenStore.findByToken(anyString())).willReturn(Optional.of(savedRefreshToken));
			given(jwtProvider.isTokenExpired(anyString())).willReturn(false);
			given(jwtProvider.extractClaims(anyString())).willReturn(claims);
			given(userReader.getActiveById(anyLong())).willReturn(user);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(newAccessToken);

			// when
			TokenResponse tokenResponse = tokenWriter.reissueToken(refreshToken);

			// then
			assertThat(tokenResponse).isNotNull();
			assertThat(tokenResponse.getAccessToken()).isEqualTo(newAccessToken);
			assertThat(tokenResponse.getRefreshToken()).isEqualTo(refreshToken);

		}

	}

}