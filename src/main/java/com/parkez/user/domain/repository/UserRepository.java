package com.parkez.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);

	Optional<User> findUserByEmail(String email);

	Optional<User> findByEmailAndRole(String email, UserRole role);
}
