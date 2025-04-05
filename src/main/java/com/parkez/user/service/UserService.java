package com.parkez.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserWriter userWriter;
    private final UserReader userReader;
}
