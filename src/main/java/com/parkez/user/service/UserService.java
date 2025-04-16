package com.parkez.user.service;

import static com.parkez.user.constant.UserConstants.*;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.service.TokenManager;
import com.parkez.common.principal.AuthUser;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.dto.request.UserChangePasswordRequest;
import com.parkez.user.dto.request.UserProfileImageUpdateRequest;
import com.parkez.user.dto.request.UserProfileUpdateRequest;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.dto.response.UserResponse;
import com.parkez.user.exception.UserErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

	@Value("${user.default-profile-image-url}")
	private String defaultProfileImageUrl;

	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	private final UserReader userReader;

	private final TokenManager tokenManager;

	public MyProfileResponse getMyProfile(AuthUser authUser) {
		User user = userReader.getActiveUserById(authUser.getId());
		return MyProfileResponse.from(user);
	}

	public UserResponse getUser(Long id) {
		User user = userReader.getActiveUserById(id);
		return UserResponse.from(user);
	}

	@Transactional
	public void updateProfile(AuthUser authUser, UserProfileUpdateRequest request) {
		if (UserRole.of(authUser.getFirstAuthority()) == UserRole.ROLE_OWNER) {
			validateBusinessInfo(request);
		}

		User user = userReader.getActiveUserById(authUser.getId());

		user.updateProfile(
			request.getNickname(),
			request.getPhone(),
			request.getBusinessNumber(),
			request.getDepositorName(),
			request.getBankName(),
			request.getBankAccount()
		);
	}

	@Transactional
	public void updateProfileImage(Long id, UserProfileImageUpdateRequest request) {

		User user = userReader.getActiveUserById(id);

		user.updateProfileImage(request.getProfileImageUrl(), defaultProfileImageUrl);
	}

	@Transactional
	public void changePassword(Long id, UserChangePasswordRequest request) {
		User user = userReader.getActiveUserById(id);
		String userPassword = user.getPassword();

		if (!bCryptPasswordEncoder.matches(request.getOldPassword(), userPassword)) {
			throw new ParkingEasyException(AuthErrorCode.INVALID_PASSWORD);
		}

		if (bCryptPasswordEncoder.matches(request.getNewPassword(), userPassword)) {
			throw new ParkingEasyException(UserErrorCode.USER_PASSWORD_SAME_AS_OLD);
		}

		String encodedPassword = bCryptPasswordEncoder.encode(request.getNewPassword());
		user.updatePassword(encodedPassword);

	}

	@Transactional
	public void deleteUser(Long id) {
		User user = userReader.getActiveUserById(id);
		user.softDelete(WITHDRAWAL_NICKNAME, LocalDateTime.now());
		tokenManager.deleteRefreshToken(id);
	}

	private void validateBusinessInfo(UserProfileUpdateRequest request) {
		if (!request.hasAllBusinessInfo()) {
			throw new ParkingEasyException(UserErrorCode.BUSINESS_INFO_REQUIRED);
		}
	}
}
