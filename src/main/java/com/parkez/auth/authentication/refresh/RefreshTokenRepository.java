package com.parkez.auth.authentication.refresh;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	void deleteByUserId(Long userId);

	boolean existsByUserId(Long userId);

	boolean existsByToken(String refreshToken);
}
