package com.parkez.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
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

	@Transactional
	public SignupResponse signupUser(SignupUserRequest request) {

		if (userReader.existUser(request.getEmail())) {
			throw new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL);
		}

		String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

		User user = User.createUser(
			request.getEmail(),
			encodedPassword,
			request.getNickname(),
			request.getPhone()
		);
		User savedUser = userWriter.create(user);
		TokenResponse tokenResponse = tokenWriter.createSignupTokenPair(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(),savedUser.getNickname());
		return SignupResponse.of(savedUser.getId(), savedUser.getEmail(), tokenResponse);

	}

}
