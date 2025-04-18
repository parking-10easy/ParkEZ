package com.parkez.payment.scheduler;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.service.PaymentReader;
import com.parkez.payment.service.PaymentWriter;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentSchedulerTest {

    @InjectMocks
    private PaymentScheduler paymentScheduler;

    @Mock
    private PaymentReader paymentReader;

    @Mock
    private PaymentWriter paymentWriter;

    @Mock
    private ReservationWriter reservationWriter;

    @Test
    void 만료된_PENDING_결제가_존재할_경우_자동_취소_및_예약_만료처리() {
        // given
        Payment payment = mock(Payment.class);
        Reservation reservation = mock(Reservation.class);

        when(payment.getReservation()).thenReturn(reservation);
        when(paymentReader.findPendingPayments(any(LocalDateTime.class))).thenReturn(List.of(payment));

        // when
        paymentScheduler.expirePendingPayments();

        // then
        verify(paymentWriter).cancelPayment(payment);
        verify(reservationWriter).expirePaymentTimeout(reservation);
    }

    @Test
    void 만료된_PENDING_결제가_없는_경우_아무_처리도_하지_않는다() {
        // given
        when(paymentReader.findPendingPayments(any(LocalDateTime.class))).thenReturn(List.of());

        // when
        paymentScheduler.expirePendingPayments();

        // then
        verify(paymentWriter, never()).cancelPayment(any());
        verify(reservationWriter, never()).expirePaymentTimeout(any());
    }


}
