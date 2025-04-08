package com.parkez.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserCommandService userCommandService;
    private final UserReader userQueryService;
}
