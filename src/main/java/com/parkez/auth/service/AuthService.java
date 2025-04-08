package com.parkez.auth.service;

import java.util.function.BiConsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenStore;
import com.parkez.auth.dto.request.SignupOwnerRequest;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.BusinessAccountInfo;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserWriter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final JwtProvider jwtProvider;
	private final RefreshTokenStore refreshTokenStore;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	@Value("${user.default-profile-image-url}")
	private String defaultProfileImageUrl;

	@Transactional
	public SignupResponse signupUser(SignupUserRequest request) {

		if (userReader.existByEmailAndRole(request.getEmail(), UserRole.ROLE_USER)) {
			throw new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL);
		}

		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		User user = User.createUser(
			request.getEmail(),
			encodedPassword,
			request.getNickname(),
			request.getPhone(),
			defaultProfileImageUrl
		);
		User savedUser = userWriter.create(user);
		TokenResponse tokenResponse = issueTokenPair(savedUser, refreshTokenStore::save);
		return SignupResponse.of(savedUser.getId(), savedUser.getEmail(), tokenResponse);

	}

	@Transactional
	public TokenResponse signinUser(String email, String password) {
		User user = userReader.getActiveByEmailAndRole(email, UserRole.ROLE_USER);

		validatePassword(password, user.getPassword());

		return issueTokenPair(user, refreshTokenStore::replace);
	}

	@Transactional
	public SignupResponse signupOwner(SignupOwnerRequest request) {

		if (userReader.existByEmailAndRole(request.getEmail(), UserRole.ROLE_OWNER)) {
			throw new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL);
		}

		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		User user = User.createOwner(
			request.getEmail(),
			encodedPassword,
			request.getNickname(),
			request.getPhone(),
			BusinessAccountInfo.create(request.getBusinessNumber(), request.getDepositorName(), request.getBankName(),
				request.getBankAccount()),
			defaultProfileImageUrl
		);

		User savedUser = userWriter.create(user);
		TokenResponse tokenResponse = issueTokenPair(savedUser, refreshTokenStore::save);
		return SignupResponse.of(savedUser.getId(), savedUser.getEmail(), tokenResponse);
	}

	@Transactional
	public TokenResponse signinOwner(String email, String password) {

		User user = userReader.getActiveByEmailAndRole(email, UserRole.ROLE_OWNER);

		validatePassword(password, user.getPassword());

		return issueTokenPair(user, refreshTokenStore::replace);


	}

	@Transactional
	public TokenResponse reissueToken(String refreshToken) {

		if (!refreshTokenStore.existsByToken(refreshToken)) {
			throw new ParkingEasyException(AuthErrorCode.TOKEN_NOT_FOUND);
		}

		if (jwtProvider.isTokenExpired(refreshToken)) {
			throw new ParkingEasyException(AuthErrorCode.TOKEN_EXPIRED);
		}

		Long userId = jwtProvider.extractUserId(refreshToken);

		User user = userReader.getActiveById(userId);

		String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleName(),
			user.getNickname());

		return TokenResponse.of(newAccessToken, refreshToken);

	}

	private void validatePassword(String password, String encodedPassword) {
		if (!bCryptPasswordEncoder.matches(password, encodedPassword)) {
			throw new ParkingEasyException(AuthErrorCode.INVALID_PASSWORD);
		}
	}

	private TokenResponse issueTokenPair(User savedUser, BiConsumer<Long, String> refreshTokenHandler) {
		String accessToken = jwtProvider.createAccessToken(savedUser.getId(), savedUser.getEmail(),
			savedUser.getRoleName(), savedUser.getNickname());
		String refreshToken = jwtProvider.createRefreshToken(savedUser.getId());
		refreshTokenHandler.accept(savedUser.getId(), refreshToken);
		return TokenResponse.of(accessToken, refreshToken);
	}

}
