package com.parkez.user.service;

import org.springframework.stereotype.Service;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;

	public User createSocialUser(String email,String encodedPassword, String nickname, LoginType loginType, UserRole role) {
		User socialUser = User.createSocialUser(email,encodedPassword, nickname, loginType, role);
		return save(socialUser);
	}

	public User createUser(String email, String encodedPassword, String nickname, String phone,
		String defaultProfileImageUrl) {
		User user = User.createUser(email, encodedPassword, nickname, phone, defaultProfileImageUrl);
		return save(user);
	}

	public User createOwner(String email, String encodedPassword, String nickname, String phone, String businessNumber,
		String depositorName, String bankName, String bankAccount, String defaultProfileImageUrl) {

		User user = User.createOwner(
			email,
			encodedPassword,
			nickname,
			phone,
			businessNumber,
			depositorName,
			bankName,
			bankAccount,
			defaultProfileImageUrl
		);
		return save(user);
	}

	public void completeSocialUserProfile(User user, String phone) {

		user.completeUserProfile(phone);

	}

	public void completeSocialOwnerProfile(User user, String phone, String businessNumber, String depositorName,
		String bankName, String bankAccount) {
		user.completeOwnerProfile(phone, businessNumber, depositorName, bankName, bankAccount);
	}



	private User save(User user) {
		return userRepository.save(user);
	}
}
