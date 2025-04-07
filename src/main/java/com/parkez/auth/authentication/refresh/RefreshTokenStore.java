package com.parkez.auth.authentication.refresh;

import java.util.Optional;

public interface RefreshTokenStore {

	void replace(Long userId, String refreshTokenValue);

	void save(Long userId, String refreshTokenValue);

	Optional<RefreshToken> findByToken(String refreshToken);
}
