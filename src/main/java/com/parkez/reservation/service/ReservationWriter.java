package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationWriter {

    private final ReservationRepository reservationRepository;

    public Reservation create(
            User user,
            ParkingZone parkingZone,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        // 요금 계산
        long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
        BigDecimal price = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLotName())
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();

        return reservationRepository.save(reservation);
    }

    public void complete(Reservation reservation) {
        reservation.complete();
    }

    public void cancel(Reservation reservation) {
        reservation.cancel();
    }

    // 예약 생성 후 10분(결제 timeout)이 지나도 상태가 PENDING 인 예약들에 대하여 자동으로 PAYMENT_EXPIRED 으로 상태 변경
    @Scheduled(fixedDelay = 60_000) // 60초 간격으로 실행
    public void expire() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        List<Reservation> expireToReservation = reservationRepository.findReservationsToExpire(expiredTime);

        if (!expireToReservation.isEmpty()) {
            expireToReservation.forEach(Reservation::expire);
            reservationRepository.saveAll(expireToReservation);
        }
    }
}
