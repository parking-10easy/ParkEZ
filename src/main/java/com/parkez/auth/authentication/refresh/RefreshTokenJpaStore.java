package com.parkez.auth.authentication.refresh;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class RefreshTokenJpaStore implements RefreshTokenStore{

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public void replace(Long userId, String refreshTokenValue) {

		if (refreshTokenRepository.existsByUserId(userId)) {
			refreshTokenRepository.deleteByUserId(userId);
		}

		RefreshToken token = RefreshToken.create(userId, refreshTokenValue);
		refreshTokenRepository.save(token);

	}

	@Override
	public void save(Long userId, String refreshTokenValue) {
		RefreshToken token = RefreshToken.create(userId, refreshTokenValue);
		refreshTokenRepository.save(token);
	}

	@Override
	public Optional<RefreshToken> findByToken(String refreshToken) {
		return refreshTokenRepository.findByToken(refreshToken);
	}

}
