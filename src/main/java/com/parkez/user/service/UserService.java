package com.parkez.user.service;

import org.springframework.stereotype.Service;

import com.parkez.auth.authentication.principal.AuthUser;
import com.parkez.user.domain.entity.User;
import com.parkez.user.dto.response.MyProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserReader userReader;

    public MyProfileResponse getMyProfile(AuthUser authUser) {
        User user = userReader.getActiveByIdAndRole(authUser.getId(), authUser.getFirstUserRole());
        return MyProfileResponse.from(user);
    }

}
