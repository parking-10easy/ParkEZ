package com.parkez.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;
import com.parkez.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserWriterTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserWriter userWriter;

	@Nested
	class CreateSocialUser {

		@Test
		public void 소셜_유저를_생성하고_저장할_수있다() {

			//given
			String email = "social@example.com";
			String nickname = "소셜닉네임";
			LoginType loginType = LoginType.KAKAO;
			UserRole role = UserRole.ROLE_USER;
			User socialUser = User.createSocialUser(email, nickname, loginType, role);

			given(userRepository.save(any(User.class))).willReturn(socialUser);
			
			//when
			User result = userWriter.createSocialUser(email, nickname, loginType, role);
			
			//then
			Assertions.assertThat(result)
				.extracting("email", "nickname", "loginType", "role", "status")
				.containsExactly(email, nickname, loginType, role, UserStatus.PENDING);

		}
	}

	@Nested
	class CreateUser {

		@Test
		public void 일반_사용자_생성하고_저장할_수있다() {
			//given
			String email = "test@example.com";
			String encodedPassword = "password";
			String nickname = "테스터";
			String phone = "010-1234-5678";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createUser(email, encodedPassword, nickname, phone, defaultProfileImageUrl);

			given(userRepository.save(any(User.class))).willReturn(user);

			//when
			User result = userWriter.createUser(email,encodedPassword,nickname,phone,defaultProfileImageUrl);

			//then
			Assertions.assertThat(result)
				.extracting("email","password", "nickname","phone","profileImageUrl","role","loginType","status")
				.containsExactly(email, encodedPassword,nickname,phone,defaultProfileImageUrl,UserRole.ROLE_USER,LoginType.NORMAL,
					UserStatus.COMPLETED);

		}
	}

	@Nested
	class CreateOwner {

		@Test
		public void 오너를_생성하고_저장할_수있다() {
			//given
			String email = "owner@example.com";
			String encodedPassword = "password";
			String nickname = "소유자";
			String phone = "010-1234-5678";
			String businessNumber = "123-45-67890";
			String depositorName = "홍길동";
			String bankName = "신한은행";
			String bankAccount = "110-9876-5432";
			String defaultProfileImageUrl = "default.jpg";
			User owner = User.createOwner(email, encodedPassword, nickname, phone, businessNumber,depositorName,bankName,bankAccount,defaultProfileImageUrl);

			given(userRepository.save(any(User.class))).willReturn(owner);

			//when
			User result = userWriter.createOwner(email,encodedPassword,nickname,phone,businessNumber,depositorName,bankName,bankAccount,defaultProfileImageUrl);

			//then
			BusinessAccountInfo businessAccountInfo = result.getBusinessAccountInfo();
			Assertions.assertThat(businessAccountInfo)
				.extracting("businessNumber", "depositorName", "bankName", "bankAccount")
				.containsExactly(businessNumber, depositorName, bankName, bankAccount);
			Assertions.assertThat(result)
				.extracting("email","password", "nickname","phone","profileImageUrl","role","loginType","status")
				.containsExactly(email, encodedPassword,nickname,phone,defaultProfileImageUrl,UserRole.ROLE_OWNER,LoginType.NORMAL,
					UserStatus.COMPLETED);

		}
	}

	@Nested
	class CompleteSocialUserProfile {
		@Test
		public void 소셜_로그인_일반사용자가_추가정보를_입력하면_상태_COMPLETED로_변경된다() {
			//given
			String email = "test@test.com";
			String nickname = "test";
			String phone = "010-1234-5667";
			User user = User.builder()
				.email(email)
				.nickname(nickname)
				.status(UserStatus.PENDING)
				.loginType(LoginType.KAKAO)
				.build();

			//when
			userWriter.completeSocialUserProfile(user, phone);

			//then
			Assertions.assertThat(user)
				.extracting("status", "phone")
				.containsExactly(UserStatus.COMPLETED, phone);
		}
	}

	@Nested
	class CompleteSocialOwnerProfile {
		@Test
		public void 소셜_로그인_오너가_추가정보를_입력하면_상태_COMPLETED로_변경된다() {
			//given
			String email = "test@test.com";
			String nickname = "test";
			String phone = "010-1234-5667";
			String businessNumber = "123-45-67890";
			String depositorName = "홍길동";
			String bankName = "신한은행";
			String bankAccount = "110-9876-5432";
			User owner = User.builder()
				.email(email)
				.nickname(nickname)
				.status(UserStatus.PENDING)
				.loginType(LoginType.KAKAO)
				.businessAccountInfo(BusinessAccountInfo.create(businessNumber,depositorName,bankName,bankAccount))
				.build();

			//when
			userWriter.completeSocialOwnerProfile(owner, phone, businessNumber,depositorName,bankName,bankAccount);

			//then
			Assertions.assertThat(owner)
				.extracting("status", "phone", "businessNumber", "depositorName", "bankName", "bankAccount")
				.containsExactly(UserStatus.COMPLETED, phone, businessNumber, depositorName, bankName, bankAccount);
		}
	}

}