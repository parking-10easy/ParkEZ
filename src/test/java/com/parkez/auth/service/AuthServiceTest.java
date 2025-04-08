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

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenJpaStore;
import com.parkez.auth.dto.request.SignupOwnerRequest;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.exception.UserErrorCode;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserWriter;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserReader userReader;

	@Mock
	private UserWriter userWriter;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenJpaStore refreshTokenStore;

	@Mock
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@InjectMocks
	private AuthService authService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(authService, "defaultProfileImageUrl", "mock-default-image-url");
	}

	@Nested
	class SignupUser {

		@Test
		public void 일반_사용자_회원가입_중복된_이메일로_가입할_수_없다() {

			//given
			String email = "user@example.com";
			String password = "1Q2w3e4r!";
			String nickname = "user";
			String phone = "010-1234-5678";
			SignupUserRequest request = createSignupUserRequest(email, password, nickname, phone);
			given(userReader.existByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(true);

			//when & then
			assertThatThrownBy(() -> authService.signupUser(request)).isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.DUPLICATED_EMAIL.getDefaultMessage());
		}

		@Test
		public void 일반_사용자_회원가입_정상적으로_가입_할_수있다() {
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
			given(userReader.existByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(false);
			given(bCryptPasswordEncoder.encode(anyString())).willReturn(encodedPassword);
			given(userWriter.create(any(User.class))).willReturn(user);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
				accessToken);
			given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

			//when
			SignupResponse signupResponse = authService.signupUser(request);

			//then
			verify(refreshTokenStore, times(1)).save(anyLong(), anyString());
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
		public void 유저_로그인_입력한_비밀번호가_다르면_예외_발생() {

			//given
			String email = "user@example.com";
			String password = "password";
			String nickname = "test";
			String phone = "1234";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createUser(email, password, nickname, phone, defaultProfileImageUrl);
			ReflectionTestUtils.setField(user, "id", 1L);
			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(false);

			//when & then
			assertThatThrownBy(() -> authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
			verify(userWriter, never()).create(any(User.class));
		}

		@Test
		public void 유저_로그인_존재하지_않은_이메일로_로그인시_예외_발생() {
			//given
			String email = "";
			String password = "password";

			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willThrow(
				new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND));

			//when & then
			assertThatThrownBy(() -> authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
			verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());
			verify(userWriter, never()).create(any(User.class));
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
			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(true);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
				accessToken);
			given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

			//when
			TokenResponse result = authService.signinUser(email, password);

			//then
			verify(refreshTokenStore, times(1)).replace(anyLong(), anyString());
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
		public void 오너_회원가입_중복된_이메일로_가입할_수_없다() {

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
			given(userReader.existByEmailAndRole(anyString(), eq(UserRole.ROLE_OWNER))).willReturn(true);

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
			User user = User.createOwner(email, password, nickname, phone, businessNumber, depositorName,
				bankName, bankAccount, defaultProfileImageUrl);
			Long userId = 1L;
			ReflectionTestUtils.setField(user, "id", userId);
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String encodedPassword = "password";

			given(userReader.existByEmailAndRole(anyString(), eq(UserRole.ROLE_OWNER))).willReturn(false);
			given(bCryptPasswordEncoder.encode(anyString())).willReturn(encodedPassword);
			given(userWriter.create(any(User.class))).willReturn(user);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
				accessToken);
			given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

			//when
			SignupResponse signupResponse = authService.signupOwner(request);

			//then
			verify(refreshTokenStore, times(1)).save(anyLong(), anyString());
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
			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_OWNER))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(false);

			//when & then
			assertThatThrownBy(() -> authService.signinOwner(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
			verify(userWriter, never()).create(any(User.class));
		}

		@Test
		public void 오너_로그인_존재하지_않은_이메일로_로그인시_예외_발생() {
			//given
			String email = "";
			String password = "password";

			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_OWNER))).willThrow(
				new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND));

			//when & then
			assertThatThrownBy(() -> authService.signinOwner(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
			verify(bCryptPasswordEncoder, never()).matches(anyString(), anyString());
			verify(userWriter, never()).create(any(User.class));

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
			ReflectionTestUtils.setField(owner, "id", 1L);
			given(userReader.getActiveByEmailAndRole(anyString(), eq(UserRole.ROLE_OWNER))).willReturn(owner);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(true);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
				accessToken);
			given(jwtProvider.createRefreshToken(anyLong())).willReturn(refreshToken);

			//when
			TokenResponse result = authService.signinOwner(email, password);

			//then
			verify(refreshTokenStore, times(1)).replace(anyLong(), anyString());
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
		void 토큰_재발급_존재하지않는_토큰이면_TOKEN_NOT_FOUND_예외발생() {
			// given
			String refreshToken = "";

			User user = User.builder()
				.email("test@example.com")
				.role(UserRole.ROLE_USER)
				.nickname("nickname")
				.build();
			ReflectionTestUtils.setField(user, "id", 1L);

			given(refreshTokenStore.existsByToken(anyString())).willReturn(false);

			// when & then
			Assertions.assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.TOKEN_NOT_FOUND.getDefaultMessage());

		}

		@Test
		void 리프레시토큰으로_재발급_만료된_토큰이면_TOKEN_EXPIRED_예외발생() {
			// given
			String expiredRefreshToken = "expired-refresh-token";

			given(refreshTokenStore.existsByToken(anyString())).willReturn(true);
			given(jwtProvider.isTokenExpired(anyString())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> authService.reissueToken(expiredRefreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.TOKEN_EXPIRED.getDefaultMessage());
		}

		@Test
		void 리프레시토큰으로_재발급_유효하지않는_토큰이면_INVALID_JWT_SIGNATURE_예외발생() {
			// given
			String invalidRefreshToken = "invalid-refresh-token";

			given(refreshTokenStore.existsByToken(anyString())).willReturn(true);
			given(jwtProvider.isTokenExpired(anyString())).willReturn(false);
			given(jwtProvider.extractUserId(anyString())).willThrow(
				new ParkingEasyException(AuthErrorCode.INVALID_JWT_SIGNATURE));
			// when & then
			assertThatThrownBy(() -> authService.reissueToken(invalidRefreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.INVALID_JWT_SIGNATURE.getDefaultMessage());
		}

		@Test
		void 리프레시토큰으로_재발급_존재하지않는_사용자면_USER_NOT_FOUND_예외발생() {
			// given
			String refreshToken = "refresh-token";
			Long userId = -1L;
			given(refreshTokenStore.existsByToken(anyString())).willReturn(true);
			given(jwtProvider.isTokenExpired(anyString())).willReturn(false);
			given(jwtProvider.extractUserId(anyString())).willReturn(userId);
			given(userReader.getActiveById(anyLong())).willThrow(
				new ParkingEasyException(UserErrorCode.USER_NOT_FOUND));
			// when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());
		}

		@Test
		void 리프레시토큰으로_재발급_탈퇴한_사용자면_USER_ALREADY_DELETED_예외발생() {
			// given
			String refreshToken = "refresh-token";
			Long userId = -1L;
			given(refreshTokenStore.existsByToken(anyString())).willReturn(true);
			given(jwtProvider.isTokenExpired(anyString())).willReturn(false);
			given(jwtProvider.extractUserId(anyString())).willReturn(userId);
			given(userReader.getActiveById(anyLong())).willThrow(
				new ParkingEasyException(UserErrorCode.USER_ALREADY_DELETED));
			// when & then
			assertThatThrownBy(() -> authService.reissueToken(refreshToken))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_ALREADY_DELETED.getDefaultMessage());
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
			ReflectionTestUtils.setField(user, "id", 1L);

			given(refreshTokenStore.existsByToken(anyString())).willReturn(true);
			given(jwtProvider.isTokenExpired(anyString())).willReturn(false);
			given(jwtProvider.extractUserId(anyString())).willReturn(userId);
			given(userReader.getActiveById(anyLong())).willReturn(user);
			given(jwtProvider.createAccessToken(anyLong(), anyString(), anyString(), anyString())).willReturn(
				newAccessToken);

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