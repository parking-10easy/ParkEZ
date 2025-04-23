package com.parkez.payment.scheduler;

import com.parkez.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentSchedulerTest {

    @InjectMocks
    private PaymentScheduler paymentScheduler;

    @Mock
    private PaymentService paymentService;

    @Test
    void payment_스케줄러_작동시_expirePayment_메서드_정상_작동() {
        // when
        paymentScheduler.expirePendingPayments();

        // then
        verify(paymentService).expirePayment();
    }
}
