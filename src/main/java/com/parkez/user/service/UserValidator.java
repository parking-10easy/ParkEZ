package com.parkez.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserValidator {

	private final UserReader userReader;

	public void validateDuplicateUser(String email, UserRole role, LoginType loginType) {
		if (userReader.existsUser(email, role, loginType)) {
			throw new ParkingEasyException(AuthErrorCode.DUPLICATED_EMAIL);
		}
	}
}
