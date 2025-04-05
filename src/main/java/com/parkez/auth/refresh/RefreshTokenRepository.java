package com.parkez.auth.refresh;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	void deleteByUserId(Long userId);

	boolean existsByUserId(Long userId);
}
