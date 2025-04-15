package com.parkez.reservation.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LockStrategy {
    NONE("noneLockService"),
    DISTRIBUTED("distributedLockService");

    private final String description;
}
