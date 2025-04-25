package com.parkez.user.domain.repository;

import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.user.domain.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;
import static com.parkez.parkingzone.domain.entity.QParkingZone.parkingZone;
import static com.parkez.payment.domain.entity.QPayment.payment;
import static com.parkez.reservation.domain.entity.QReservation.reservation;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<User> findOwnersForSettlementByMonth(YearMonth yearMonth, Pageable pageable) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        return jpaQueryFactory
                .selectDistinct(parkingLot.owner)
                .from(payment)
                .join(payment.reservation, reservation)
                .join(reservation.parkingZone, parkingZone)
                .join(parkingZone.parkingLot, parkingLot)
                .where(
                        payment.paymentStatus.eq(PaymentStatus.APPROVED),
                        reservation.status.eq(ReservationStatus.COMPLETED),
                        reservation.endDateTime.between(start, end)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
