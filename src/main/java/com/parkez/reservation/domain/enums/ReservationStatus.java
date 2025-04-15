package com.parkez.reservation.domain.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    PENDING("결제 대기 중인 예약"),
    CONFIRMED("예약 완료 됨"),
    COMPLETED("사용 완료 됨"),
    CANCELED("예약 취소 됨"),
    PAYMENT_EXPIRED("결제 요청 시간 만료 된 예약")
    ;

    private final String description;
}