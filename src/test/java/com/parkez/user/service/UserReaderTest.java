package com.parkez.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import com.parkez.user.exception.UserErrorCode;

@ExtendWith(MockitoExtension.class)
class UserReaderTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserReader userReader;

	@Nested
	class Exist {

		@Test
		public void 이메일_존재_시_true_반환() {
			//given
			String email = "test@example.com";
			given(userRepository.existsByEmail(anyString())).willReturn(true);
			//when
			boolean result = userReader.exist(email);
			//then
			assertThat(result).isTrue();
		}

		@Test
		public void 이메일_존재하지_않을_시_false_반환() {
			//given
			String email = "test@example.com";
			given(userRepository.existsByEmail(anyString())).willReturn(false);
			//when
			boolean result = userReader.exist(email);
			//then
			assertThat(result).isFalse();
		}
	}

	@Nested
	class GetActiveByEmailAndRole {

		@Test
		public void 이메일_역할_일치_탈퇴하지않은_사용자_정상조회() {
			//given
			String email = "test@example.com";
			UserRole role = UserRole.ROLE_USER;
			User user = User.builder()
				.email(email)
				.nickname("테스트 유저")
				.phone("010-1234-5678")
				.build();
			ReflectionTestUtils.setField(user, "role", role);
			ReflectionTestUtils.setField(user, "deletedAt", null); // 탈퇴 안 한 상태
			given(userRepository.findByEmailAndRole(anyString(), any(UserRole.class))).willReturn(Optional.of(user));
			//when
			User result = userReader.getActiveByEmailAndRole(email, role);
			//then
			assertThat(result).isEqualTo(user);
		}

		@Test
		public void 이메일_역할_일치하는_사용자_없으면_EMAIL_NOT_FOUND_예외() {
			//given
			String email = "test@example.com";
			UserRole role = UserRole.ROLE_USER;
			given(userRepository.findByEmailAndRole(anyString(), any(UserRole.class))).willReturn(Optional.empty());
			//when & then
			assertThatThrownBy(() -> userReader.getActiveByEmailAndRole(email, role))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
		}

		@Test
		public void 이메일_역할_일치하지만_탈퇴한_사용자이면_USER_ALREADY_DELETED_예외() {
			//given
			String email = "test@example.com";
			UserRole role = UserRole.ROLE_USER;
			User user = User.builder()
				.email(email)
				.nickname("테스트 유저")
				.phone("010-1234-5678")
				.build();
			ReflectionTestUtils.setField(user, "role", role);
			ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
			given(userRepository.findByEmailAndRole(anyString(), any(UserRole.class))).willReturn(Optional.of(user));
			//when & then
			assertThatThrownBy(() -> userReader.getActiveByEmailAndRole(email, role))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_ALREADY_DELETED.getDefaultMessage());
		}
	}

	@Nested
	class GetActiveByIdAndRole {

		@Test
		public void 아이디_역할_일치_탈퇴하지않은_사용자_정상조회() {
			//given
			Long id = 1L;
			UserRole role = UserRole.ROLE_USER;
			User user = User.builder()
				.nickname("테스트 유저")
				.phone("010-1234-5678")
				.build();
			ReflectionTestUtils.setField(user, "id", id);
			ReflectionTestUtils.setField(user, "role", role);
			ReflectionTestUtils.setField(user, "deletedAt", null); // 탈퇴 안 한 상태
			given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
			//when
			User result = userReader.getActiveById(id);
			//then
			assertThat(result).isEqualTo(user);
		}

		@Test
		public void 아이디_역할_일치하는_사용자_없으면_USER_NOT_FOUND_예외() {
			//given
			Long id = 1L;
			UserRole role = UserRole.ROLE_USER;
			given(userRepository.findById(anyLong())).willReturn(Optional.empty());
			//when & then
			assertThatThrownBy(() -> userReader.getActiveById(id))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());
		}

		@Test
		public void 아이디_역할_일치하지만_탈퇴한_사용자이면_USER_ALREADY_DELETED_예외() {
			//given
			Long id = 1L;
			UserRole role = UserRole.ROLE_USER;
			User user = User.builder()
				.nickname("테스트 유저")
				.phone("010-1234-5678")
				.build();
			ReflectionTestUtils.setField(user, "id", id);
			ReflectionTestUtils.setField(user, "role", role);
			ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
			given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
			//when & then
			assertThatThrownBy(() -> userReader.getActiveById(id))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(UserErrorCode.USER_ALREADY_DELETED.getDefaultMessage());
		}
	}

}