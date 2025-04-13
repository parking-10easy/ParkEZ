package com.parkez.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;

@ExtendWith(MockitoExtension.class)
class TokenManagerTest {

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenStore refreshTokenStore;

	@InjectMocks
	private TokenManager tokenManager;

	@Nested
	class IssueTokens {

		@Test
		public void 유저_정보로_accessToken과_refreshTokend을_발급하고_refreshToken을_저장한다() {
			//given
			long userId = 1L;
			String email = "user@example.com";
			String nickname = "nickname";
			UserRole userRole = UserRole.ROLE_USER;
			LoginType loginType = LoginType.NORMAL;
			UserStatus userStatus = UserStatus.COMPLETED;
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(userRole)
				.loginType(loginType)
				.status(userStatus)
				.build();
			ReflectionTestUtils.setField(user,"id", userId);

			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";

			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
				.willReturn(accessToken);
			given(jwtProvider.createRefreshToken(userId)).willReturn(refreshToken);
			doNothing().when(refreshTokenStore).set(anyLong(),anyString());

			//when
			TokenResponse tokenResponse = tokenManager.issueTokens(user);

			//then
			assertThat(tokenResponse)
				.extracting("accessToken", "refreshToken")
				.containsExactly(accessToken, refreshToken);

		}
	}

	@Nested
	class ExtractUserId {

		@Test
		public void refreshToken의_subject가_null이면_INVALID_JWT_SIGNATURE_예외를_던진다() {
			// given
			String refreshToken = "mockRefreshToken";

			given(jwtProvider.extractUserId(anyString())).willThrow(
				new ParkingEasyException(AuthErrorCode.INVALID_JWT_SIGNATURE)
			);

			// when & then
			assertThatThrownBy(() -> tokenManager.extractUserId(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_JWT_SIGNATURE.getDefaultMessage());

		}

		@Test
		public void refreshToken_파싱_중_예외가_발생하면_INVALID_JWT_SIGNATURE_예외를_던진다() {
			// given
			String refreshToken = "mockRefreshToken";

			given(jwtProvider.extractUserId(anyString())).willThrow(
				new ParkingEasyException(AuthErrorCode.INVALID_JWT_SIGNATURE)
			);

			// when & then
			assertThatThrownBy(() -> tokenManager.extractUserId(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_JWT_SIGNATURE.getDefaultMessage());

		}

		@Test
		public void refreshToken에서_정상적으로_userId를_추출한다() {
			//given
			String refreshToken = "mockRefresh";
			Long expectedUserId = 1L;

			given(jwtProvider.extractUserId(anyString())).willReturn(expectedUserId);

			//when
			Long userId = tokenManager.extractUserId(refreshToken);

			//then
			assertThat(userId).isEqualTo(expectedUserId);

		}
	}

	@Nested
	class ValidateRefreshToken {

		@Test
		public void userId에_해당하는_refreshToken이_존재하면_예외를_던지지_않는다() {
			//given
			Long userId = 1L;
			given(refreshTokenStore.existsBy(userId)).willReturn(true);

			//when & then
			assertThatCode(() -> tokenManager.validateRefreshToken(userId))
				.doesNotThrowAnyException();

		}

		@Test
		public void userId에_해당하는_refreshToken이_존재하지_않으면_TOKEN_NOT_FOUND_예외를_던진다() {
			//given
			Long userId = 1L;
			given(refreshTokenStore.existsBy(userId)).willReturn(false);

			//when & then
			assertThatThrownBy(() -> tokenManager.validateRefreshToken(userId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.TOKEN_NOT_FOUND.getDefaultMessage());


		}
	}

	@Nested
	class ReissueAccessToken {

		@Test
		public void 유저_정보로_accessToken을_재발급할_수_있다() {
			//given
			long userId = 1L;
			String email = "user@example.com";
			String nickname = "nickname";
			UserRole userRole = UserRole.ROLE_USER;
			LoginType loginType = LoginType.NORMAL;
			UserStatus userStatus = UserStatus.COMPLETED;
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(userRole)
				.loginType(loginType)
				.status(userStatus)
				.build();
			ReflectionTestUtils.setField(user,"id", userId);
			String expectedAccessToken = "mockAccess";

			given(jwtProvider.createAccessToken(anyLong(),anyString(),anyString(),anyString(),anyBoolean())).willReturn(expectedAccessToken);

			//when
			String accessToken = tokenManager.reissueAccessToken(user);

			//then
			assertThat(accessToken).isEqualTo(expectedAccessToken);

		}
	}

}