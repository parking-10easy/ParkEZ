package com.parkez.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.authentication.principal.AuthUser;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
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

    private final UserReader userReader;

    public MyProfileResponse getMyProfile(AuthUser authUser) {
        User user = userReader.getActiveById(authUser.getId());
        return MyProfileResponse.from(user);
    }

    public UserResponse getUser(Long id) {
        User user = userReader.getActiveById(id);
        return UserResponse.from(user);
    }

    @Transactional
    public void updateProfile(AuthUser authUser, UserProfileUpdateRequest request) {
        if (authUser.getFirstUserRole() == UserRole.ROLE_OWNER) {
            validateBusinessInfo(request);
        }

        User user = userReader.getActiveById(authUser.getId());

        user.updateProfile(
            request.getNickname(),
            request.getPhone(),
            request.getBusinessNumber(),
            request.getBankName(),
            request.getBankAccount(),
            request.getDepositorName()
        );
    }

    private void validateBusinessInfo(UserProfileUpdateRequest request) {
        if (request.getBusinessNumber() == null
            || request.getBankName() == null
            || request.getBankAccount() == null
            || request.getDepositorName() == null) {
            throw new ParkingEasyException(UserErrorCode.BUSINESS_INFO_REQUIRED);
        }
    }

    public void updateProfileImage(Long id, UserProfileImageUpdateRequest request) {

        User user = userReader.getActiveById(id);

        user.updateProfileImage(request.getProfileImageUrl(), defaultProfileImageUrl);
    }
}
