package com.parkez.payment.domain.repository;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;

import java.time.YearMonth;
import java.util.List;

public interface PaymentQueryDslRepository {

    List<Payment> findApprovedAndCompletedPayments(User owner, YearMonth month);

    Payment getApprovedPaymentWithCompletedReservation(User owner, Long reservationId);


}
