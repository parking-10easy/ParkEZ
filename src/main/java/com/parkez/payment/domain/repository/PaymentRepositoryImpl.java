package com.parkez.payment.domain.repository;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.user.domain.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;
import static com.parkez.parkingzone.domain.entity.QParkingZone.parkingZone;
import static com.parkez.payment.domain.entity.QPayment.payment;
import static com.parkez.reservation.domain.entity.QReservation.reservation;



@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentQueryDslRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Payment> findApprovedAndCompletedPayments(User owner, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

        return queryFactory
                .selectFrom(payment)
                .join(payment.reservation, reservation)
                .join(reservation.parkingZone, parkingZone)
                .join(parkingZone.parkingLot, parkingLot)
                .where(
                        payment.paymentStatus.eq(PaymentStatus.APPROVED),
                        reservation.status.eq(ReservationStatus.COMPLETED),
                        parkingLot.owner.eq(owner),
                        reservation.endDateTime.between(start, end)
                )
                .fetch();
    }

    @Override
    public Payment getApprovedPaymentWithCompletedReservation(User owner, Long reservationId) {
        return queryFactory
                .selectFrom(payment)
                .join(payment.reservation, reservation).fetchJoin()
                .join(reservation.parkingZone, parkingZone).fetchJoin()
                .join(parkingZone.parkingLot, parkingLot).fetchJoin()
                .where(
                        reservation.id.eq(reservationId),
                        reservation.status.eq(ReservationStatus.COMPLETED),
                        payment.paymentStatus.eq(PaymentStatus.APPROVED),
                        parkingLot.owner.eq(owner)
                )
                .fetchOne();
    }

}
