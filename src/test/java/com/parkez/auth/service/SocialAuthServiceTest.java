package com.parkez.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.parkez.auth.dto.request.SocialOwnerProfileCompleteRequest;
import com.parkez.auth.dto.request.SocialUserProfileCompleteRequest;
import com.parkez.auth.dto.response.SocialSignupCompleteResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.client.OAuthClient;
import com.parkez.auth.oauth.client.OauthClientAdapter;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;
import com.parkez.user.exception.UserErrorCode;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserWriter;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

	@Mock
	private OauthClientAdapter oauthClientAdapter;

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private TokenManager tokenManager;

	@Mock
	private OAuthClient oAuthClient;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private SocialAuthService socialAuthService;

	@Nested
	class Login {

		@Test
		public void 소셜_로그인_존재하는_계정으로_토큰을_생성하고_반환한다() {
			//given
			String state = "user";
			String code = "code";
			String email = "test@kakao.com";
			String nickname = "testuser";
			OAuthProvider provider = OAuthProvider.KAKAO;
			UserRole role = UserRole.fromState(state);
			LoginType loginType = LoginType.from(provider.name());
			UserStatus userStatus = UserStatus.PENDING;
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(role)
				.loginType(loginType)
				.status(userStatus)
				.build();
			OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
				.id(1L)
				.email("test@kakao.com")
				.nickname("testuser")
				.provider(OAuthProvider.KAKAO)
				.build();
			String accessToken = "token";
			String refreshToken = "refreshToken";
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

			given(oauthClientAdapter.getClient(any(OAuthProvider.class))).willReturn(oAuthClient);
			given(oAuthClient.requestUserInfo(anyString())).willReturn(oAuthUserInfo);
			given(userReader.findActiveUser(anyString(), any(UserRole.class), any(LoginType.class))).willReturn(
				Optional.of(user));
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);

			//when
			TokenResponse result = socialAuthService.login(provider, code, state);

			//then
			Assertions.assertThat(result)
				.extracting(
					"accessToken",
					"refreshToken"
				).containsExactly(
					accessToken,
					refreshToken
				);

		}

		@Test
		public void 소셜_로그인_존재하지_않는_유저는_새로_생성하고_토큰을_반환한다() {
			//given
			String state = "user";
			String code = "code";
			String email = "test@kakao.com";
			String nickname = "testuser";
			String password = "password";
			OAuthProvider provider = OAuthProvider.KAKAO;
			UserRole role = UserRole.fromState(state);
			LoginType loginType = LoginType.from(provider.name());
			UserStatus userStatus = UserStatus.PENDING;
			User newUser = User.builder()
				.email(email)
				.nickname(nickname)
				.role(role)
				.loginType(loginType)
				.status(userStatus)
				.build();
			OAuthUserInfo oAuthUserInfo = OAuthUserInfo.builder()
				.id(1L)
				.email("test@kakao.com")
				.nickname("testuser")
				.provider(OAuthProvider.KAKAO)
				.build();
			String accessToken = "token";
			String refreshToken = "refreshToken";
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

			given(oauthClientAdapter.getClient(any(OAuthProvider.class))).willReturn(oAuthClient);
			given(oAuthClient.requestUserInfo(anyString())).willReturn(oAuthUserInfo);
			given(userReader.findActiveUser(anyString(), any(UserRole.class), any(LoginType.class))).willReturn(
				Optional.empty());
			given(passwordEncoder.encode(anyString())).willReturn(password);
			given(userWriter.createSocialUser(anyString(),anyString(),anyString(),any(LoginType.class),any(UserRole.class))).willReturn(newUser);
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);

			//when
			TokenResponse result = socialAuthService.login(provider, code, state);

			//then
			Assertions.assertThat(result)
				.extracting(
					"accessToken",
					"refreshToken"
				).containsExactly(
					accessToken,
					refreshToken
				);

		}
	}

	@Nested
	class CompleteUserSignup {

		@Test
		public void 소셜_유저_프로필을_완료하고_토큰을_재발급한다() {
			//given

			long userId = 1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialUserProfileCompleteRequest request = SocialUserProfileCompleteRequest.builder().phone(phone).build();
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.KAKAO)
				.status(UserStatus.PENDING)
				.build();

			given(userReader.getActiveUserById(anyLong())).willReturn(user);
			String newAccessToken = "newAccessToken";
			given(tokenManager.reissueAccessToken(user)).willReturn(newAccessToken);

			//when
			SocialSignupCompleteResponse socialSignupCompleteResponse = socialAuthService.completeUserSignup(authUser,
				request);

			//then
			Assertions.assertThat(socialSignupCompleteResponse)
				.extracting("accessToken")
				.isEqualTo(newAccessToken);

		}

		@Test
		public void 존재하지_않는_유저이면_USER_NOT_FOUND_예외를_던진다() {
			//given

			long userId = 1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialUserProfileCompleteRequest request = SocialUserProfileCompleteRequest.builder().phone(phone).build();

			given(userReader.getActiveUserById(anyLong())).willThrow(
				new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
			);

			//when & then

			assertThatThrownBy(() -> socialAuthService.completeUserSignup(authUser, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 이미_가입_완료인_소설_유저는_ALREADY_COMPLETED_예외를_던진다() {
			//given

			long userId = -1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialUserProfileCompleteRequest request = SocialUserProfileCompleteRequest.builder().phone(phone).build();
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.KAKAO)
				.status(UserStatus.COMPLETED)
				.build();
			String newAccessToken = "newAccessToken";

			given(userReader.getActiveUserById(anyLong())).willReturn(user);

			//when & then

			assertThatThrownBy(() -> socialAuthService.completeUserSignup(authUser, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.ALREADY_COMPLETED.getDefaultMessage());

		}
	}

	@Nested
	class CompleteOwnerSignup {

		@Test
		public void 소셜_오너_프로필을_완료하고_토큰을_재발급한다() {
			//given

			long userId = 1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialOwnerProfileCompleteRequest request = SocialOwnerProfileCompleteRequest.builder()
				.phone(phone)
				.bankAccount("123456-78-910111")
				.businessNumber("123-45-67890")
				.depositorName("홍길동")
				.bankName("국민은행")
				.build();
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.KAKAO)
				.status(UserStatus.PENDING)
				.build();
			String newAccessToken = "newAccessToken";

			given(userReader.getActiveUserById(anyLong())).willReturn(user);
			given(tokenManager.reissueAccessToken(user)).willReturn(newAccessToken);

			//when
			SocialSignupCompleteResponse socialSignupCompleteResponse = socialAuthService.completeOwnerSignup(authUser,
				request);

			//then
			Assertions.assertThat(socialSignupCompleteResponse)
				.extracting("accessToken")
				.isEqualTo(newAccessToken);

		}

		@Test
		public void 존재하지_않는_유저이면_USER_NOT_FOUND_예외를_던진다() {
			//given

			long userId = -1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialOwnerProfileCompleteRequest request = SocialOwnerProfileCompleteRequest.builder()
				.phone(phone)
				.bankAccount("123456-78-910111")
				.businessNumber("123-45-67890")
				.depositorName("홍길동")
				.bankName("국민은행")
				.build();

			given(userReader.getActiveUserById(anyLong())).willThrow(
				new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
			);

			//when & then

			assertThatThrownBy(() -> socialAuthService.completeOwnerSignup(authUser, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 이미_가입_완료인_소설_오너는_ALREADY_COMPLETED_예외를_던진다() {
			//given

			long userId = 1L;
			String email = "test@example.com";
			String nickname = "nickname";
			String roleName = "ROLE_USER";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.nickname(nickname)
				.roleName(roleName)
				.build();
			String phone = "010-1234-5678";
			SocialOwnerProfileCompleteRequest request = SocialOwnerProfileCompleteRequest.builder()
				.phone(phone)
				.bankAccount("123456-78-910111")
				.businessNumber("123-45-67890")
				.depositorName("홍길동")
				.bankName("국민은행")
				.build();
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.KAKAO)
				.status(UserStatus.COMPLETED)
				.build();
			String newAccessToken = "newAccessToken";

			given(userReader.getActiveUserById(anyLong())).willReturn(user);

			//when & then

			assertThatThrownBy(() -> socialAuthService.completeOwnerSignup(authUser, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.ALREADY_COMPLETED.getDefaultMessage());

		}
	}

}