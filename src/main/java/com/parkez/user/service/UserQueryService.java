package com.parkez.user.service;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("유저 없음")
        );
    }
}
