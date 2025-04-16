package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
