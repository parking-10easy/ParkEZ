package com.parkez.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.auth.authentication.principal.AuthUser;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.dto.request.UserProfileUpdateRequest;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.dto.response.UserResponse;
import com.parkez.user.exception.UserErrorCode;

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
				.profileImageUrl(profileImageUrl)
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

	@Nested
	class UpdateProfile {

		@Test
		void 프로필_수정_오너_사업자_정보_없으면_예외가_발생한다() {
			// given
			long userId = 1L;
			UserRole userRole = UserRole.ROLE_OWNER;
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.userRole(userRole)
				.build();

			String updateNickname = "홍길동";
			String updatePhone = "010-1234-5678";
			UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
				.nickname(updateNickname)
				.phone(updatePhone)
				.build();

			// when & then
			Assertions.assertThatThrownBy(() -> userService.updateProfile(authUser, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.BUSINESS_INFO_REQUIRED.getDefaultMessage());
		}

		@Test
		public void 프로필_수정_오너_정상적으로_수정할_수_있다() {
			//given
			long userId = 1L;
			UserRole userRole = UserRole.ROLE_OWNER;
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.userRole(userRole)
				.build();
			String updateNickname = "홍길동";
			String updatePhone = "010-1234-5678";
			String updateBusinessNumber = "123-45-67890";
			String updateBankName = "국민은행";
			String updateBankAccount = "123456-78-901234";
			String updateDepositorName = "홍길동";
			UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
				.nickname(updateNickname)
				.phone(updatePhone)
				.businessNumber(updateBusinessNumber)
				.bankName(updateBankName)
				.bankAccount(updateBankAccount)
				.depositorName(updateDepositorName)
				.build();
			User user = User.builder()
				.nickname("홍길동1")
				.phone("011-1234-5678")
				.businessAccountInfo(new BusinessAccountInfo(null,null,null,null))
				.build();
			given(userReader.getActiveById(anyLong())).willReturn(user);
			//when
			userService.updateProfile(authUser, request);
			//then
			Assertions.assertThat(user)
				.extracting(
					"nickname",
					"phone",
					"businessAccountInfo.businessNumber",
					"businessAccountInfo.bankName",
					"businessAccountInfo.bankAccount",
					"businessAccountInfo.depositorName"
				)
				.containsExactly(
					updateNickname,
					updatePhone,
					updateBusinessNumber,
					updateBankName,
					updateBankAccount,
					updateDepositorName
				);
		}

		@Test
		public void 프로필_수정_일반_사용자_정상적으로_수정할_수_있다() {
			//given
			long userId = 1L;
			UserRole userRole = UserRole.ROLE_USER;
			AuthUser authUser = AuthUser.builder()
				.id(userId)
				.userRole(userRole)
				.build();
			String updateNickname = "홍길동";
			String updatePhone = "010-1234-5678";
			UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
				.nickname(updateNickname)
				.phone(updatePhone)
				.build();
			User user = User.builder()
				.nickname("홍길동1")
				.phone("011-1234-5678")
				.businessAccountInfo(new BusinessAccountInfo(null,null,null,null))
				.build();
			given(userReader.getActiveById(anyLong())).willReturn(user);
			//when
			userService.updateProfile(authUser, request);
			//then
			Assertions.assertThat(user)
				.extracting(
					"nickname",
					"phone",
					"businessAccountInfo.businessNumber",
					"businessAccountInfo.bankName",
					"businessAccountInfo.bankAccount",
					"businessAccountInfo.depositorName"
				)
				.containsExactly(
					updateNickname,
					updatePhone,
					null,
					null,
					null,
					null
				);
		}
	}

}