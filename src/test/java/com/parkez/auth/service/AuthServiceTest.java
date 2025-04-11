package com.parkez.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.dto.request.SignupOwnerRequest;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.exception.UserErrorCode;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserValidator;
import com.parkez.user.service.UserWriter;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private UserValidator userValidator;

	@Mock
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Mock
	private TokenManager tokenManager;

	@InjectMocks
	private AuthService authService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(authService, "defaultProfileImageUrl", "mock-default-image-url");
	}

	@Nested
	class SignupUser {

		@Test
		public void 일반_사용자_회원가입_중복된_이메일_권한_로그인타입이면_DUPLICATED_EMAIL_예외_발생() {

			//given
			String email = "user@example.com";
			String password = "1Q2w3e4r!";
			String nickname = "user";
			String phone = "010-1234-5678";
			SignupUserRequest request = createSignupUserRequest(email, password, nickname, phone);
			doThrow(new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL))
				.when(userValidator)
				.validateDuplicateUser(anyString(), eq(UserRole.ROLE_USER), eq(LoginType.NORMAL));

			//when & then
			assertThatThrownBy(() -> authService.signupUser(request)).isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.DUPLICATED_EMAIL.getDefaultMessage());
		}

		@Test
		public void 일반_사용자_회원가입_LoginType_NORMAL로_정상적으로_가입_할_수있다() {
			//given
			String email = "user@example.com";
			String password = "1Q2w3e4r!";
			String nickname = "user";
			String phone = "010-1234-5678";
			String defaultProfileImageUrl = "default.jpg";
			SignupUserRequest request = createSignupUserRequest(email, password, nickname, phone);
			Long userId = 1L;
			User user = User.createUser(email, password, nickname, phone, defaultProfileImageUrl);
			ReflectionTestUtils.setField(user, "id", userId);
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String encodedPassword = "password";
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
			doNothing().when(userValidator)
				.validateDuplicateUser(anyString(), eq(UserRole.ROLE_USER), eq(LoginType.NORMAL));
			given(bCryptPasswordEncoder.encode(anyString())).willReturn(encodedPassword);
			given(userWriter.createUser(anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(user);
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);

			//when
			SignupResponse signupResponse = authService.signupUser(request);

			//then
			assertThat(signupResponse).extracting(
				"id",
				"email",
				"accessToken",
				"refreshToken"
			).containsExactly(
				userId,
				email,
				accessToken,
				refreshToken
			);
		}
	}

	@Nested
	class SigninUser {

		@Test
		public void 유저_로그인_존재하지_않은_이메일로_로그인시_예외_발생() {
			//given
			String email = "";
			String password = "password";

			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_USER), eq(LoginType.NORMAL))).willThrow(
				new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND)
			);

			//when & then
			assertThatThrownBy(() -> authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
			verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());
			verify(tokenManager, never()).issueTokens(any(User.class));
		}

		@Test
		public void 유저_로그인_입력한_비밀번호가_다르면_예외_발생() {

			//given
			String email = "user@example.com";
			String password = "password";
			String nickname = "test";
			String phone = "1234";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createUser(email, password, nickname, phone, defaultProfileImageUrl);
			ReflectionTestUtils.setField(user, "id", 1L);
			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_USER), eq(LoginType.NORMAL))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(false);

			//when & then
			assertThatThrownBy(() -> authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
			verify(tokenManager, never()).issueTokens(any(User.class));
		}

		@Test
		public void 유저_로그인_정상적으로_로그인_할_수_있다() {
			//given
			String email = "user@example.com";
			String password = "password";
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String nickname = "test";
			String phone = "1234";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createUser(email, password, nickname, phone, defaultProfileImageUrl);
			ReflectionTestUtils.setField(user, "id", 1L);
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_USER), eq(LoginType.NORMAL))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(true);
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);

			//when
			TokenResponse result = authService.signinUser(email, password);

			//then
			assertThat(result)
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
	class SignupOwner {

		@Test
		public void 오너_회원가입_중복된_이메일_권한_로그인타입이면_DUPLICATED_EMAIL_예외_발생() {

			//given
			String duplicatedEmail = "owner@example.com";
			String password = "1Q2w3e4r!";
			String nickname = "owner";
			String phone = "010-1234-5678";
			String businessNumber = "123-45-67890";
			String bankName = "국민은행";
			String bankAccount = "123456-78-901234";
			String depositorName = "홍길동";
			SignupOwnerRequest request = createSignupOwnerRequest(duplicatedEmail, password, nickname, phone,
				bankAccount,
				businessNumber,
				bankName, depositorName);
			given(userReader.existsUser(anyString(), eq(UserRole.ROLE_OWNER), eq(LoginType.NORMAL))).willReturn(true);

			//when & then
			assertThatThrownBy(() -> authService.signupOwner(request)).isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.DUPLICATED_EMAIL.getDefaultMessage());
		}

		@Test
		public void 오너_회원가입_정상적으로_가입_할_수있다() {

			//given
			String email = "owner@example.com";
			String password = "1Q2w3e4r!";
			String nickname = "owner";
			String phone = "010-1234-5678";
			String businessNumber = "123-45-67890";
			String bankName = "국민은행";
			String bankAccount = "123456-78-901234";
			String depositorName = "홍길동";
			String defaultProfileImageUrl = "default.jpg";
			SignupOwnerRequest request = createSignupOwnerRequest(email, password, nickname, phone, bankAccount,
				businessNumber,
				bankName, depositorName);
			User owner = User.createOwner(email, password, nickname, phone, businessNumber, depositorName,
				bankName, bankAccount, defaultProfileImageUrl);
			Long userId = 1L;
			ReflectionTestUtils.setField(owner, "id", userId);
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String encodedPassword = "password";
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

			given(userReader.existsUser(anyString(), eq(UserRole.ROLE_OWNER), eq(LoginType.NORMAL))).willReturn(false);
			given(bCryptPasswordEncoder.encode(anyString())).willReturn(encodedPassword);
			given(userWriter.createOwner(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).willReturn(owner);
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);

			//when
			SignupResponse signupResponse = authService.signupOwner(request);

			//then
			assertThat(signupResponse).extracting(
				"id",
				"email",
				"accessToken",
				"refreshToken"
			).containsExactly(
				userId,
				email,
				accessToken,
				refreshToken
			);
		}
	}

	@Nested
	class SigninOwner {

		@Test
		public void 오너_로그인_입력한_비밀번호가_다르면_예외_발생() {

			//given
			String email = "owner@example.com";
			String password = "password";
			String nickname = "test";
			String phone = "1234";
			String businessNumber = "123-45-67890";
			String bankName = "국민은행";
			String bankAccount = "123456-78-901234";
			String depositorName = "홍길동";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createOwner(email, password, nickname, phone, businessNumber, depositorName,
				bankName, bankAccount, defaultProfileImageUrl);
			ReflectionTestUtils.setField(user, "id", 1L);
			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_OWNER), eq(LoginType.NORMAL))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(false);

			//when & then
			assertThatThrownBy(() -> authService.signinOwner(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
		}

		@Test
		public void 오너_로그인_존재하지_않은_이메일로_로그인시_예외_발생() {
			//given
			String email = "";
			String password = "password";

			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_OWNER), eq(LoginType.NORMAL))).willThrow(
				new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND));

			//when & then
			assertThatThrownBy(() -> authService.signinOwner(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
			verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());

		}

		@Test
		public void 오너_로그인_정상적으로_로그인_할_수_있다() {

			//given
			String email = "owner@example.com";
			String password = "password";
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String nickname = "test";
			String phone = "1234";
			String bankName = "국민은행";
			String businessNumber = "1234-455758-567";
			String bankAccount = "123456-78-901234";
			String depositorName = "홍길동";
			String defaultProfileImageUrl = "default.jpg";
			User owner = User.createOwner(email, password, nickname, phone, businessNumber, depositorName,
				bankName, bankAccount, defaultProfileImageUrl);
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
			ReflectionTestUtils.setField(owner, "id", 1L);

			given(userReader.getActiveUser(anyString(), eq(UserRole.ROLE_OWNER), eq(LoginType.NORMAL))).willReturn(owner);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(true);
			given(tokenManager.issueTokens(any(User.class))).willReturn(tokenResponse);


			//when
			TokenResponse result = authService.signinOwner(email, password);

			//then
			assertThat(result)
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
	class ReissueToken {



		@Test
		void 리프레시토큰으로_재발급_만료된_토큰이면_TOKEN_EXPIRED_예외발생() {
			// given
			String expiredRefreshToken = "expired-refresh-token";

			given(tokenManager.extractUserId(anyString())).willThrow(new ParkingEasyException(AuthErrorCode.TOKEN_EXPIRED));

			// when & then
			assertThatThrownBy(() -> authService.reissueToken(expiredRefreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.TOKEN_EXPIRED.getDefaultMessage());
			verify(tokenManager, never()).validateRefreshToken(anyLong());
			verify(userReader, never()).getActiveUserById(anyLong());
			verify(tokenManager, never()).reissueAccessToken(any(User.class));
		}

		@Test
		void 리프레시토큰으로_재발급_유효하지않는_토큰이면_INVALID_JWT_SIGNATURE_예외발생() {
			// given
			String invalidRefreshToken = "invalid-refresh-token";


			given(tokenManager.extractUserId(anyString())).willThrow(new ParkingEasyException(AuthErrorCode.INVALID_JWT_SIGNATURE));

			// when & then
			assertThatThrownBy(() -> authService.reissueToken(invalidRefreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.INVALID_JWT_SIGNATURE.getDefaultMessage());
			verify(tokenManager, never()).validateRefreshToken(anyLong());
			verify(userReader, never()).getActiveUserById(anyLong());
			verify(tokenManager, never()).reissueAccessToken(any(User.class));
		}

		@Test
		void 리프레시토큰으로_재발급_존재하지않는_토큰이면_TOKEN_NOT_FOUND_예외발생() {
			// given
			String refreshToken = "";
			Long userId = 1L;
			User user = User.builder()
				.email("test@example.com")
				.role(UserRole.ROLE_USER)
				.nickname("nickname")
				.build();
			ReflectionTestUtils.setField(user, "id", userId);

			given(tokenManager.extractUserId(anyString())).willReturn(userId);
			doThrow(new ParkingEasyException(AuthErrorCode.TOKEN_NOT_FOUND))
				.when(tokenManager).validateRefreshToken(anyLong());


			// when & then
			Assertions.assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.TOKEN_NOT_FOUND.getDefaultMessage());
			verify(userReader, never()).getActiveUserById(anyLong());
			verify(tokenManager, never()).reissueAccessToken(any(User.class));

		}

		@Test
		void 리프레시토큰으로_재발급_존재하지않는_사용자면_USER_NOT_FOUND_예외발생() {
			// given
			String refreshToken = "refresh-token";
			Long userId = -1L;
			given(tokenManager.extractUserId(anyString())).willReturn(userId);
			doNothing().when(tokenManager).validateRefreshToken(anyLong());
			given(userReader.getActiveUserById(anyLong())).willThrow(
				new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
			);


			// when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());
			verify(tokenManager, never()).reissueAccessToken(any(User.class));
		}

		@Test
		void 리프레시토큰_재발급_신규_accessToken_발급_성공() {
			// given
			String refreshToken = "refresh-token";
			String newAccessToken = "new-access-token";
			Long userId = 1L;

			User user = User.builder()
				.email("test@example.com")
				.role(UserRole.ROLE_USER)
				.nickname("nickname")
				.build();
			ReflectionTestUtils.setField(user, "id", 1L);

			given(tokenManager.extractUserId(anyString())).willReturn(userId);
			doNothing().when(tokenManager).validateRefreshToken(anyLong());
			given(userReader.getActiveUserById(anyLong())).willReturn(user);
			given(tokenManager.reissueAccessToken(any(User.class))).willReturn(newAccessToken);

			// when
			TokenResponse tokenResponse = authService.reissueToken(refreshToken);
			// then
			assertThat(tokenResponse)
				.extracting(
					"accessToken",
					"refreshToken"
				).containsExactly(
					newAccessToken,
					refreshToken
				);

		}

	}

	private static SignupUserRequest createSignupUserRequest(String email, String password, String nickname,
		String phone) {
		return SignupUserRequest.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.phone(phone)
			.build();
	}

	private static SignupOwnerRequest createSignupOwnerRequest(String email, String password, String nickname,
		String phone,
		String bankAccount, String businessNumber, String bankName, String depositorName) {
		return SignupOwnerRequest.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.phone(phone)
			.bankAccount(bankAccount)
			.businessNumber(businessNumber)
			.bankName(bankName)
			.depositorName(depositorName)
			.build();
	}

}