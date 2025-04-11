package com.parkez.user.service;

import static com.parkez.user.constant.UserConstants.*;
import static org.assertj.core.api.Assertions.*;
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

import com.parkez.common.principal.AuthUser;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.dto.request.UserChangePasswordRequest;
import com.parkez.user.dto.request.UserProfileImageUpdateRequest;
import com.parkez.user.dto.request.UserProfileUpdateRequest;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.dto.response.UserResponse;
import com.parkez.user.exception.UserErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserReader userReader;

	@Mock
	private BCryptPasswordEncoder bCryptPasswordEncoder;

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
				.roleName(userRole.name())
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
			given(userReader.getActiveUserById(anyLong())).willReturn(user);
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
			given(userReader.getActiveUserById(anyLong())).willReturn(user);
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
				.roleName(userRole.name())
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
				.roleName(userRole.name())
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
				.businessAccountInfo(BusinessAccountInfo.create(null, null, null, null))
				.build();
			given(userReader.getActiveUserById(anyLong())).willReturn(user);
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
				.roleName(userRole.name())
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
				.businessAccountInfo(BusinessAccountInfo.create(null, null, null, null))
				.build();
			given(userReader.getActiveUserById(anyLong())).willReturn(user);
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

	@Nested
	class UpdateProfileImage {

		@Test
		void 프로필_이미지_수정_URL_없을_때_기본이미지_설정() {
			// given
			Long userId = 1L;
			String profileImageUrl = "";
			UserProfileImageUpdateRequest request = new UserProfileImageUpdateRequest();
			ReflectionTestUtils.setField(request, "profileImageUrl", profileImageUrl);
			String defaultProfileImageUrl = "default.jpg";
			User user = User.builder()
				.profileImageUrl(profileImageUrl)
				.build();
			ReflectionTestUtils.setField(user, "id", userId);
			ReflectionTestUtils.setField(userService, "defaultProfileImageUrl", "default.jpg");
			given(userReader.getActiveUserById(anyLong())).willReturn(user);

			// when
			userService.updateProfileImage(userId, request);

			// then
			verify(userReader).getActiveUserById(userId);
			Assertions.assertThat(user)
				.extracting(
					"profileImageUrl"
				).isEqualTo(defaultProfileImageUrl);
		}

		@Test
		void 프로필_이미지_수정_URL_입력_있을_때_수정_성공() {
			// given
			Long userId = 1L;
			String profileImageUrl = "https://image.com/profile.jpg";
			UserProfileImageUpdateRequest request = new UserProfileImageUpdateRequest();
			ReflectionTestUtils.setField(request, "profileImageUrl", profileImageUrl);

			User user = User.builder()
				.profileImageUrl(profileImageUrl)
				.build();
			ReflectionTestUtils.setField(user, "id", userId);
			given(userReader.getActiveUserById(anyLong())).willReturn(user);

			// when
			userService.updateProfileImage(userId, request);

			// then
			verify(userReader).getActiveUserById(userId);
			Assertions.assertThat(user)
				.extracting(
					"profileImageUrl"
				).isEqualTo(profileImageUrl);

		}
	}

	@Nested
	class ChangePassword {
		@Test
		public void 비밀번호_변경_정상적으로_수행할_수_있다() {
			//given
			Long userId = 1L;
			String oldPassword = "oldPassword123";
			String newPassword = "newPassword456";
			String encodedOldPassword = "encodedOldPassword";
			String encodedNewPassword = "encodedNewPassword";
			User user = User.builder()
				.password(encodedOldPassword)
				.build();
			given(userReader.getActiveUserById(userId)).willReturn(user);
			given(bCryptPasswordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(true);
			given(bCryptPasswordEncoder.matches(newPassword, encodedOldPassword)).willReturn(false);
			given(bCryptPasswordEncoder.encode(newPassword)).willReturn(encodedNewPassword);
			UserChangePasswordRequest request = UserChangePasswordRequest.builder()
				.oldPassword(oldPassword)
				.newPassword(newPassword)
				.build();
			//when
			userService.changePassword(userId, request);
			//then
			Assertions.assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
		}

		@Test
		public void 비밀번호_변경_실패_기존비밀번호_틀림() {
			//given
			Long userId = 1L;
			String oldPassword = "wrongOldPassword";
			String newPassword = "newPassword456";
			String encodedOldPassword = "encodedOldPassword";
			User user = User.builder()
				.password(encodedOldPassword)
				.build();
			given(userReader.getActiveUserById(userId)).willReturn(user);
			given(bCryptPasswordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(false);
			UserChangePasswordRequest request = UserChangePasswordRequest.builder()
				.oldPassword(oldPassword)
				.newPassword(newPassword)
				.build();
			//when & then
			Assertions.assertThatThrownBy(()->userService.changePassword(userId, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(AuthErrorCode.INVALID_PASSWORD.getDefaultMessage());
		}

		@Test
		public void 비밀번호_변경_실패_새비밀번호가_기존과_같음() {
			//given
			Long userId = 1L;
			String oldPassword = "oldPassword123";
			String newPassword = "oldPassword123";
			String encodedOldPassword = "encodedOldPassword";
			User user = User.builder()
				.password(encodedOldPassword)
				.build();
			given(userReader.getActiveUserById(userId)).willReturn(user);
			given(bCryptPasswordEncoder.matches(oldPassword, encodedOldPassword)).willReturn(true);
			given(bCryptPasswordEncoder.matches(newPassword, encodedOldPassword)).willReturn(true);
			UserChangePasswordRequest request = UserChangePasswordRequest.builder()
				.oldPassword(oldPassword)
				.newPassword(newPassword)
				.build();
			//when & then
			Assertions.assertThatThrownBy(()->userService.changePassword(userId, request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(UserErrorCode.USER_PASSWORD_SAME_AS_OLD.getDefaultMessage());
		}
	}

	@Nested
	class DeleteUser {
		@Test
		void 회원탈퇴_소프트딜리트_성공() {
			// given
			Long userId = 1L;
			User user = User.builder()
				.nickname("기존 닉네임")
				.build();

			given(userReader.getActiveUserById(anyLong())).willReturn(user);

			// when
			userService.deleteUser(userId);

			// then
			Assertions.assertThat(user.getNickname()).isEqualTo(WITHDRAWAL_NICKNAME);
			Assertions.assertThat(user.isDeleted()).isTrue();
			Assertions.assertThat(user.getDeletedAt()).isNotNull();
		}
	}

}