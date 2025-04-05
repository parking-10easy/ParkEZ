package com.parkez.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.authentication.jwt.TokenWriter;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
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
	private TokenWriter tokenWriter;

	@Mock
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@InjectMocks
	private AuthService authService;

	@Nested
	class SignupUser {


		@Test
		public void 일반_사용자_회원가입_중복된_이메일로_가입할_수_없다() {
			//given
			String email = "user@example.com";
			String password = "1Q2w3e4r!";
			String passwordCheck = "1Q2w3e4r!";
			String nickname = "user";
			String phone = "010-1234-5678";
			SignupUserRequest request = createSignupUserRequest(email, password, passwordCheck, nickname, phone);
			given(userReader.exist(anyString())).willReturn(true);
			//when & then
			assertThatThrownBy(()-> authService.signupUser(request)).isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.DUPLICATED_EMAIL.getDefaultMessage());
		}

		@Test
		public void 일반_사용자_회원가입_정상적으로_가입_할_수있다() {
			//given
			String email = "user@example.com";
			String password = "1Q2w3e4r!";
			String passwordCheck = "1Q2w3e4r!";
			String nickname = "user";
			String phone = "010-1234-5678";
			SignupUserRequest request = createSignupUserRequest(email, password, passwordCheck, nickname, phone);
			User user = User.createUser(email, password, nickname, phone);
			Long userId = 1L;
			ReflectionTestUtils.setField(user, "id", userId);
			String accessToken = "mockAccess";
			String refreshToken = "mockRefresh";
			String encodedPassword = "password";
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
			given(userReader.exist(anyString())).willReturn(false);
			given(bCryptPasswordEncoder.encode(anyString())).willReturn(encodedPassword);

			given(userWriter.create(any(User.class))).willReturn(user);
			given(tokenWriter.createSignupTokenPair(
				anyLong(),
				anyString(),
				any(UserRole.class),
				anyString()
			)).willReturn(tokenResponse);
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
	class Signin {

		@Test
		public void 유저_로그인_입력한_비밀번호가_다르면_예외_발생() {
			//given
			String email = "user@example.com";
			String password = "password";
			String nickname = "test";
			String phone = "1234";
			User user = User.createUser(email,password,nickname,phone);
			ReflectionTestUtils.setField(user,"id", 1L);
			given(userReader.getByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(),anyString())).willReturn(false);
			//when & then
			assertThatThrownBy(() ->authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
			verify(userWriter,never()).create(any(User.class));
			verify(tokenWriter,never()).createSignupTokenPair(anyLong(),anyString(),any(UserRole.class),anyString());
		}

		@Test
		public void 유저_로그인_존재하지_않은_이메일로_로그인시_예외_발생() {
			//given
			String email = "user@example.com";
			String password = "password";

			given(userReader.getByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willThrow(new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND));
			//when & then
			assertThatThrownBy(() ->authService.signinUser(email, password))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
			verify(bCryptPasswordEncoder,never()).matches(anyString(), anyString());
			verify(userWriter,never()).create(any(User.class));
			verify(tokenWriter,never()).createSignupTokenPair(anyLong(),anyString(),any(UserRole.class),anyString());
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
			User user = User.createUser(email,password,nickname,phone);
			ReflectionTestUtils.setField(user,"id", 1L);
			TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
			given(userReader.getByEmailAndRole(anyString(), eq(UserRole.ROLE_USER))).willReturn(user);
			given(bCryptPasswordEncoder.matches(anyString(), anyString())).willReturn(true);
			given(tokenWriter.createSigninTokenPair(anyLong(),anyString(),any(UserRole.class),anyString())).willReturn(tokenResponse);
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

	private static SignupUserRequest createSignupUserRequest(String email, String password, String passwordCheck, String nickname,
		String phone) {
		return SignupUserRequest.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.phone(phone)
			.build();
	}
}