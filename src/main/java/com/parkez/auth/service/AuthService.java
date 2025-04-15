package com.parkez.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.dto.request.SignupOwnerRequest;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserWriter;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final TokenManager tokenManager;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	@Value("${user.default-profile-image-url}")
	private String defaultProfileImageUrl;

	@Transactional
	public SignupResponse signupUser(SignupUserRequest request) {

		validateDuplicateUser(request.getEmail(), UserRole.ROLE_USER, LoginType.NORMAL);

		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		User user = userWriter.createUser(request.getEmail(), encodedPassword, request.getNickname(), request.getPhone(),
			defaultProfileImageUrl);

		TokenResponse tokenResponse = tokenManager.issueTokens(user);

		return SignupResponse.of(user.getId(), user.getEmail(), tokenResponse);

	}

	@Transactional
	public TokenResponse signinUser(String email, String password) {

		User user = userReader.getActiveUser(email, UserRole.ROLE_USER, LoginType.NORMAL);

		validatePassword(password, user.getPassword());

		return tokenManager.issueTokens(user);

	}

	@Transactional
	public SignupResponse signupOwner(SignupOwnerRequest request) {

		validateDuplicateUser(request.getEmail(), UserRole.ROLE_OWNER, LoginType.NORMAL);

		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		User user = userWriter.createOwner(request.getEmail(), encodedPassword, request.getNickname(),
			request.getPhone(), request.getBusinessNumber(), request.getDepositorName(), request.getBankName(),
			request.getBankAccount(), defaultProfileImageUrl);

		TokenResponse tokenResponse = tokenManager.issueTokens(user);

		return SignupResponse.of(user.getId(), user.getEmail(), tokenResponse);
	}

	@Transactional
	public TokenResponse signinOwner(String email, String password) {

		User user = userReader.getActiveUser(email, UserRole.ROLE_OWNER, LoginType.NORMAL);

		validatePassword(password, user.getPassword());

		return tokenManager.issueTokens(user);

	}

	@Transactional
	public TokenResponse reissueToken(String refreshToken) {
		Long userId = tokenManager.extractUserId(refreshToken);

		tokenManager.validateRefreshTokenExists(userId);

		User user = userReader.getActiveUserById(userId);

		String newAccessToken = tokenManager.reissueAccessToken(user);

		return TokenResponse.of(newAccessToken, refreshToken);

	}

	private void validatePassword(String password, String encodedPassword) {
		if (!bCryptPasswordEncoder.matches(password, encodedPassword)) {
			throw new ParkingEasyException(AuthErrorCode.INVALID_PASSWORD);
		}
	}

	private void validateDuplicateUser(String email, UserRole role, LoginType loginType) {
		if (userReader.existsUser(email, role, loginType)) {
			throw new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL);
		}
	}

}
