package com.parkez.auth.authentication.refresh;

public interface RefreshTokenStore {

	void replace(Long userId, String refreshTokenValue);

	void save(Long userId, String refreshTokenValue);

	boolean existsByToken(String refreshToken);
}
