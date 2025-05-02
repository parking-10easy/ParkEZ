package com.parkez.payment.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentType;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWriterTest {

    @InjectMocks
    private PaymentWriter paymentWriter;

    @Mock
    private PaymentRepository paymentRepository;

    @Nested
    class CreatePayment {

        @Test
        void 정상적으로_결제요청을_생성하면_저장된_Payment_반환() {
            // given
            User user = mock(User.class);
            Reservation reservation = mock(Reservation.class);
            String orderId = "test-order-id";

            Payment savedPayment = mock(Payment.class);
            given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);

            // when
            Payment result = paymentWriter.createPayment(user, reservation, orderId);

            // then
            assertThat(result).isEqualTo(savedPayment);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    class SavePayment {

        @Test
        void 결제_승인정보를_업데이트하고_저장() {
            // given
            Payment payment = mock(Payment.class);
            PaymentConfirmResponse response = mock(PaymentConfirmResponse.class);

            given(response.getPaymentKey()).willReturn("payKey");
            given(response.getApprovedAt()).willReturn("2025-04-16T12:34:56");
            given(response.getType()).willReturn(PaymentType.NORMAL);

            // when
            paymentWriter.savePayment(payment, response);

            // then
            verify(payment).approvePaymentInfo("payKey", "2025-04-16T12:34:56", PaymentType.NORMAL);
        }
    }

    @Nested
    class CancelPayment {

        @Test
        void 결제를_취소하면_상태가_변경되고_저장() {
            // given
            Payment payment = mock(Payment.class);

            // when
            paymentWriter.cancelPayment(payment);

            // then
            verify(payment).cancel(any(LocalDateTime.class));
        }
    }
}

