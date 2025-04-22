package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReaderTest {

    @InjectMocks
    private PaymentReader paymentReader;

    @Mock
    private PaymentRepository paymentRepository;

    @Nested
    class GetPayment {

        @Test
        void 주문번호로_결제내역_정상조회() {
            // given
            String orderId = "test-order-id";
            Payment payment = mock(Payment.class);

            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

            // when
            Payment result = paymentReader.getPayment(orderId);

            // then
            assertThat(result).isEqualTo(payment);
        }

        @Test
        void 주문번호로_결제내역_조회시_없으면_PAYMENT_NOT_FOUND_예외_발생() {
            // given
            String orderId = "non-existent-order";
            given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

            // when & then
            ParkingEasyException ex = assertThrows(ParkingEasyException.class,
                    () -> paymentReader.getPayment(orderId));

            assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    class FindByReservation {

        @Test
        void 예약정보로_결제_정상_조회() {
            // given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(paymentRepository.findByReservation(reservation)).willReturn(Optional.of(payment));

            // when
            Optional<Payment> result = paymentReader.findByReservation(reservation);

            // then
            assertThat(result).isPresent().contains(payment);
        }
    }

    @Nested
    class findPendingPayments{
        @Test
        void 만료기준시간_이전_Pending_결제들을_조회한다() {
            // given
            LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
            List<Payment> expectedPayments = List.of(mock(Payment.class));

            when(paymentRepository.findPendingPaymentsToExpire(expiredTime))
                    .thenReturn(expectedPayments);

            // when
            List<Payment> result = paymentReader.findPendingPayments(expiredTime);

            // then
            assertThat(result).isEqualTo(expectedPayments);
            verify(paymentRepository).findPendingPaymentsToExpire(expiredTime);
        }

    }

    @Nested
    class getApprovedPaymentWithCompletedReservation {
        @Test
        void 완료된_예약에_대한_결제_정보를_조회한다() {
            // given
            User owner = mock(User.class);
            Long reservationId = 1L;
            Payment payment = mock(Payment.class);

            when(paymentRepository.getApprovedPaymentWithCompletedReservation(owner, reservationId)).thenReturn(payment);

            // when
            Payment result = paymentReader.getApprovedPaymentWithCompletedReservation(owner, reservationId);

            // then
            assertThat(result).isEqualTo(payment);
        }
    }

    @Nested
    class findApprovedAndCompletedPayments {
        @Test
        void 특정_월의_결제완료_예약완료_내역을_조회한다() {
            // given
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);
            List<Payment> payments = List.of(mock(Payment.class));

            when(paymentRepository.findApprovedAndCompletedPayments(owner, month)).thenReturn(payments);

            // when
            List<Payment> result = paymentReader.findApprovedAndCompletedPayments(owner, month);

            // then
            assertThat(result).isEqualTo(payments);
        }
    }
}
