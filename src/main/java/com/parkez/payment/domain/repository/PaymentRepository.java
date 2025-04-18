package com.parkez.payment.domain.repository;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.reservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByReservation(Reservation reservation);

    @Query(""" 
            SELECT p
            FROM Payment p
            WHERE p.paymentStatus = 'PENDING'
              AND p.createdAt < :expiredTime
            """)
    List<Payment> findPendingPaymentsToExpire(LocalDateTime expiredTime);
}
