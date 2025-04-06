package com.parkez.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.authentication.principal.AuthUser;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.dto.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserReader userReader;

	@InjectMocks
	private UserService userService;

	@Nested
	class GetMyProfile {

		@Test
		public void 내_프로필_조회_성공() {
			//given

			long userId = 1L;
			String email = "test@test.com";
			UserRole userRole = UserRole.ROLE_USER;
			String nickname = "테스트 유저";
			String phone = "010-1234-5678";
			String profileImageUrl = "default.jpg";
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.email(email)
				.userRole(userRole)
				.nickname(nickname)
				.build();
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.phone(phone)
				.build();
			ReflectionTestUtils.setField(user, "id", userId);
			ReflectionTestUtils.setField(user, "role", userRole);
			ReflectionTestUtils.setField(user, "profileImageUrl", profileImageUrl);
			given(userReader.getActiveById(anyLong())).willReturn(user);
			//when
			MyProfileResponse myProfileResponse = userService.getMyProfile(authUser);
			//then
			assertThat(myProfileResponse)
				.extracting(
					"id",
					"email",
					"nickname",
					"phone",
					"profileImageUrl"
				).containsExactly(
					userId,
					email,
					nickname,
					phone,
					profileImageUrl
				);

			assertThat(myProfileResponse.getBusinessNumber()).isNull();
			assertThat(myProfileResponse.getBankName()).isNull();
			assertThat(myProfileResponse.getBankAccount()).isNull();
			assertThat(myProfileResponse.getDepositorName()).isNull();

		}
	}

	@Nested
	class GetUser {
		@Test
		public void 유저_조회_성공() {
			//given
			long userId = 1L;
			String nickname = "테스트 유저";
			String phone = "010-1234-5678";
			String profileImageUrl = "default.jpg";
			User user = User.builder()
				.nickname(nickname)
				.phone(phone)
				.build();
			ReflectionTestUtils.setField(user, "id", userId);
			given(userReader.getActiveById(anyLong())).willReturn(user);
			//when
			UserResponse userResponse = userService.getUser(userId);
			//then
			assertThat(userResponse)
				.extracting(
					"id",
					"nickname",
					"profileImageUrl"
				)
				.containsExactly(
					userId,
					nickname,
					profileImageUrl
				);
		}
	}


}