package com.parkez.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmailAndRole(String email, UserRole role);

	Optional<User> findByEmailAndRole(String email, UserRole role);

	Optional<User> findById(Long id);
}
