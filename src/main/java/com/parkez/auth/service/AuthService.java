package com.parkez.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.authentication.jwt.TokenWriter;
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

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

	private final UserReader userReader;
	private final UserWriter userWriter;
	private final TokenWriter tokenWriter;
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
		TokenResponse tokenResponse = tokenWriter.createSignupTokenPair(savedUser.getId(), savedUser.getEmail(),
			savedUser.getRole(), savedUser.getNickname());
		return SignupResponse.of(savedUser.getId(), savedUser.getEmail(), tokenResponse);

	}

	@Transactional
	public TokenResponse signinUser(String email, String password) {
		User user = userReader.getActiveByEmailAndRole(email, UserRole.ROLE_USER);

		validatePassword(password, user.getPassword());

		return tokenWriter.createSigninTokenPair(user.getId(), user.getEmail(), user.getRole(), user.getNickname());
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
		TokenResponse tokenResponse = tokenWriter.createSignupTokenPair(savedUser.getId(), savedUser.getEmail(),
			savedUser.getRole(), savedUser.getNickname());
		return SignupResponse.of(savedUser.getId(), savedUser.getEmail(), tokenResponse);
	}

	@Transactional
	public TokenResponse signinOwner(String email, String password) {

		User user = userReader.getActiveByEmailAndRole(email, UserRole.ROLE_OWNER);

		validatePassword(password, user.getPassword());

		return tokenWriter.createSigninTokenPair(user.getId(), user.getEmail(), user.getRole(), user.getNickname());

	}

	private void validatePassword(String password, String encodedPassword) {
		if (!bCryptPasswordEncoder.matches(password, encodedPassword)) {
			throw new ParkingEasyException(AuthErrorCode.INVALID_PASSWORD);
		}
	}

}
