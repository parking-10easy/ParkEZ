package com.parkez.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserWriter {

	private final UserRepository userRepository;
	public User create(User user) {

		return userRepository.save(user);

	}
}
