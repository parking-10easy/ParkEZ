package com.parkez.alarm.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String token;

    private boolean status;

    public FcmDevice(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.status = true;
    }

    public static FcmDevice of(Long userId, String token) {
        return new FcmDevice(userId, token);
    }

    public void updateStatus(boolean status) {
        this.status = status;
    }
}
