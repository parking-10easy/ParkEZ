package com.parkez.user.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ParkingEasyException(ReservationErrorCode.NOT_FOUND_USER)
        );
    }
}
