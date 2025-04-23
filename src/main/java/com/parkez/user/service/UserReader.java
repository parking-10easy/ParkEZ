package com.parkez.user.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        return userRepository.existsByEmailAndRoleAndLoginType(email, role, loginType);
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

    public Optional<User> findActiveUser(String email, UserRole role, LoginType loginType) {
        return userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(email, role, loginType);
    }

    public User getUserByEmailAndRole(String email, UserRole role) {
        return userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role).orElseThrow(
                () -> new ParkingEasyException(UserErrorCode.USER_NOT_FOUND)
        );
    }

    public List<User> findAllOwnersByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return userRepository.findAllByRole(UserRole.ROLE_OWNER, pageable).getContent();
    }
}
