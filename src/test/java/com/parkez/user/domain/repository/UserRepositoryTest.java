package com.parkez.user.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		String email = "test@example.com";
		String password = "1234";
		String nickname = "테스터";
		String phone = "010-1234-5678";
		User user = User.createUser(email, password, nickname, phone);
		userRepository.save(user);
	}

	@Test
	public void 이메일로_존재여부_확인() {
		//when
		boolean existsTrue = userRepository.existsByEmail("test@example.com");
		boolean existsFalse = userRepository.existsByEmail("test1@example.com");
		//then
		assertThat(existsTrue).isTrue();
		assertThat(existsFalse).isFalse();
	}

	@Test
	public void 이메일_역할로_조회() {
		//given
		UserRole role = UserRole.ROLE_USER;
		//when
		Optional<User> result = userRepository.findByEmailAndRole("test@example.com", role);
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
	public void 아이디_역할로_조회() {
		//given
		UserRole role = UserRole.ROLE_USER;
		//when
		Optional<User> result = userRepository.findByIdAndRole(1L, role);
		//then
		assertThat(result).isPresent();
		assertThat(result.get()).extracting(
			"id",
			"password",
			"nickname",
			"phone"
		).containsExactly(
			1L,
			"1234",
			"테스터",
			"010-1234-5678"
		);
	}
}