package com.parkez.auth.service;

import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
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
			given(userReader.existUser(anyString())).willReturn(true);
			//when & then
			Assertions.assertThatThrownBy(()-> authService.signupUser(request)).isInstanceOf(ParkingEasyException.class)
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
			given(userReader.existUser(anyString())).willReturn(false);
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
			Assertions.assertThat(signupResponse).extracting(
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