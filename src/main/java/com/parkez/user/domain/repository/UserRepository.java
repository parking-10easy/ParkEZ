package com.parkez.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
}
