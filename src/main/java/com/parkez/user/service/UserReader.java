package com.parkez.user.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import com.parkez.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

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

    public List<User> findOwnersForSettlementByMonth(YearMonth targetMonth, Long lastId, int size) {
        return userRepository.findAllOwnersForSettlementByMonth(targetMonth, lastId, size);
    }
}
