package com.parkez.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

	@Mock
	private UserReader userReader;

	@InjectMocks
	private UserValidator userValidator;

	@Nested
	class ValidateDuplicateUser {

		@Test
		public void 이메일_역할_로그인_타입이_같은_유저가_존재하면_DUPLICATED_EMAIL_예외를_던진다() {
			//given
			String email = "duple@exampl.com";
			UserRole role = UserRole.ROLE_USER;
			LoginType loginType = LoginType.NORMAL;

			given(userReader.existsUser(anyString(),any(UserRole.class), any(LoginType.class))).willReturn(true);

			//when & then
			assertThatThrownBy(()->userValidator.validateDuplicateUser(email,role,loginType))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.DUPLICATED_EMAIL.getDefaultMessage());

		}

		@Test
		public void 이메일_역할_로그인_타입이_같은_유저가_존재하지_않으면_정상_수행된다() {
			//given
			String email = "new@exampl.com";
			UserRole role = UserRole.ROLE_USER;
			LoginType loginType = LoginType.NORMAL;

			given(userReader.existsUser(anyString(),any(UserRole.class), any(LoginType.class))).willReturn(false);

			//when & then
			assertThatCode(()->userValidator.validateDuplicateUser(email,role,loginType))
				.doesNotThrowAnyException();

		}
	}

}