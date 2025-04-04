package com.parkez.user.domain.enums;

import com.parkez.common.exception.ParkingEasyException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_OWNER(Authority.OWNER),
    ROLE_USER(Authority.USER);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new ParkingEasyException(null));
    }

    public static class Authority {
        public static final String OWNER = "ROLE_OWNER";
        public static final String USER = "ROLE_USER";
    }
}