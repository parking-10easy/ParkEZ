package com.parkez.reservation.domain.enums;

import java.util.Arrays;

public enum LockStrategy {
    DEFAULT;

    public static LockStrategy from(String key) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown strategy: " + key));
    }
}
