package com.parkez.user.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.common.config.PersistenceConfig;
import com.parkez.common.config.QueryDslConfig;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import({PersistenceConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;


	@Nested
	class ExistsByEmailAndRoleAndLoginType {

		@Test
		public void 이메일_역할_로그인_타입에_해당하는_유저가_있으면_True를_반환한다() {
			//given
			String email = "test@example.com";
			String password = "1234";
			String nickname = "테스터";
			String phone = "010-1234-5678";
			String defaultProfileImageUrl = "default.jpg";
			User user = User.createUser(email, password, nickname, phone,defaultProfileImageUrl);
			userRepository.save(user);

			//when
			boolean existsTrue = userRepository.existsByEmailAndRoleAndLoginType("test@example.com", UserRole.ROLE_USER, LoginType.NORMAL);

			//then
			assertThat(existsTrue).isTrue();
		}

		@Test
		public void 이메일_역할_로그인_타입에_해당하는_유저가_없으면_false를_반환한다() {

			//when
			boolean existsTrue = userRepository.existsByEmailAndRoleAndLoginType("test@example.com", UserRole.ROLE_USER, LoginType.KAKAO);

			//then
			assertThat(existsTrue).isFalse();
		}
	}

	@Nested
	class FindByEmailAndRoleAndLoginTypeAndDeletedAtIsNull {

		@Test
		public void 이메일_역할_로그인타입으로_조회할_수있다() {
			//given
			User user = User.builder()
				.email("test@example.com")
				.password("1234")
				.nickname("테스터")
				.phone("010-1234-5678")
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.NORMAL)
				.status(UserStatus.COMPLETED)
				.build();
			userRepository.save(user);
			//when

			Optional<User> result = userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(
				"test@example.com", UserRole.ROLE_USER, LoginType.NORMAL
			);
			//then
			assertThat(result).isPresent();
			assertThat(result.get()).extracting(
				"email",
				"password",
				"nickname",
				"phone"
			).containsExactly(
				"test@example.com",
				"1234",
				"테스터",
				"010-1234-5678"
			);
		}

		@Test
		void 유저가_존재하지_않으면_Optional_empty를_반환한다() {
			// when
			Optional<User> foundUser = userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(
				"notfound@example.com", UserRole.ROLE_USER, LoginType.NORMAL
			);

			// then
			assertThat(foundUser).isEmpty();
		}

	}

	@Nested
	class FindByIdAndDeletedAtIsNull {

		@Test
		public void 아이디로_유저를_조회할_수_있다() {
			//given
			User user = User.builder()
				.email("test4@example.com")
				.password("password")
				.nickname("nickname")
				.phone("010-1234-5678")
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.NORMAL)
				.status(UserStatus.COMPLETED)
				.build();
			User savedUser = userRepository.save(user);

			//when
			Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId());

			//then
			assertThat(result).isPresent();
			assertThat(result.get()).extracting(
				"id",
				"password",
				"nickname",
				"phone"
			).containsExactly(
				savedUser.getId(),
				"password",
				"nickname",
				"010-1234-5678"
			);
		}

		@Test
		void 삭제된_유저는_조회할_수_없다() {
			// given
			User user = User.builder()
				.email("test5@example.com")
				.password("1234")
				.nickname("삭제된테스터")
				.phone("010-5678-1234")
				.role(UserRole.ROLE_USER)
				.loginType(LoginType.NORMAL)
				.status(UserStatus.COMPLETED)
				.build();
			user.softDelete("탈퇴한_유저", LocalDateTime.now());
			User savedUser = userRepository.save(user);

			// when
			Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(savedUser.getId());

			// then
			assertThat(result).isNotPresent();
		}

		@Test
		void 아이디로_유저_조회시_존재하지_않으면_empty를_반환한다() {
			//given
			Long userId = -1L;

			// when
			Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(userId);

			// then
			assertThat(result).isEmpty();
		}

	}

	@Nested
	class FindByEmailAndRoleAndDeletedAtIsNull {

		@Test
		void 이메일과_역할로_삭제되지_않은_유저를_조회할_수_있다() {
			// given
			String email = "admin@parkez.com";
			UserRole role = UserRole.ROLE_ADMIN;

			User user = User.builder()
					.email(email)
					.password("password123")
					.nickname("관리자")
					.phone("010-0000-0000")
					.role(role)
					.loginType(LoginType.NORMAL)
					.status(UserStatus.COMPLETED)
					.build();

			User savedUser = userRepository.save(user);

			// when
			Optional<User> result = userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role);

			// then
			assertThat(result).isPresent();
			assertThat(result.get()).extracting(
					"email", "password", "nickname", "role"
			).containsExactly(
					"admin@parkez.com", "password123", "관리자", UserRole.ROLE_ADMIN
			);
		}

		@Test
		void 삭제된_유저는_이메일과_역할로_조회할_수_없다() {
			// given
			String email = "deleted@parkez.com";
			UserRole role = UserRole.ROLE_ADMIN;

			User user = User.builder()
					.email(email)
					.password("delete123")
					.nickname("삭제된관리자")
					.phone("010-1111-1111")
					.role(role)
					.loginType(LoginType.NORMAL)
					.status(UserStatus.COMPLETED)
					.build();
			user.softDelete("관리자 탈퇴", LocalDateTime.now());

			userRepository.save(user);

			// when
			Optional<User> result = userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role);

			// then
			assertThat(result).isNotPresent();
		}

		@Test
		void 이메일이나_역할이_일치하지_않으면_empty를_반환한다() {
			// given
			String email = "notfound@parkez.com";
			UserRole role = UserRole.ROLE_ADMIN;

			// when
			Optional<User> result = userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role);

			// then
			assertThat(result).isEmpty();
		}
	}

}