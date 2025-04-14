package com.parkez.reservation.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    PENDING("예약 대기 중"),
    CONFIRMED("예약 완료 됨"),
    COMPLETED("사용 완료 됨"),
    CANCELED("예약 취소 됨")
    ;

    private final String description;
}