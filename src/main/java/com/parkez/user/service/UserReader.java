package com.parkez.user.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import com.parkez.user.exception.UserErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReader {

	private final UserRepository userRepository;

	public boolean exist(String email) {
		return userRepository.existsByEmail(email);
	}

	public User getActiveByEmailAndRole(String email, UserRole role) {
		User user = userRepository.findByEmailAndRole(email, role).orElseThrow(
			() -> new ParkingEasyException(UserErrorCode.EMAIL_NOT_FOUND)
		);
		validateActiveUser(user);
		return user;
	}

	public User getActiveById(Long id) {
		User user = userRepository.findById(id).orElseThrow(
			() -> new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
		);
		validateActiveUser(user);
		return user;

	}

	private static void validateActiveUser(User user) {
		if (user.isDeleted()) {
			throw new ParkingEasyException(UserErrorCode.USER_ALREADY_DELETED);
		}
	}

}
