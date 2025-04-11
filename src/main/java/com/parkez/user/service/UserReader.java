package com.parkez.user.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import com.parkez.user.exception.UserErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReader {

	private final UserRepository userRepository;

	public boolean existsUser(String email, UserRole role, LoginType loginType) {
		return userRepository.existsByEmailAndRoleAndLoginType(email,role, loginType);
	}

	public User getActiveUser(String email, UserRole role, LoginType loginType) {
		return userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(email, role, loginType).orElseThrow(
			() -> new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND)
		);
	}

	public User getActiveUserById(Long id) {
		return userRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
			() -> new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
		);

	}

}
