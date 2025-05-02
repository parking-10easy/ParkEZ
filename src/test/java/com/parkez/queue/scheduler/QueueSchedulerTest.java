package com.parkez.queue.scheduler;

import com.parkez.payment.scheduler.PaymentScheduler;
import com.parkez.payment.service.PaymentService;
import com.parkez.queue.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QueueSchedulerTest {

    @InjectMocks
    private QueueScheduler queueScheduler;

    @Mock
    private QueueService queueService;

    @Test
    void deleteQueues_스케줄러_작동시_deleteExpiredQueues_메서드_정상_작동() {
        // when
        queueScheduler.deleteQueues();

        // then
        verify(queueService).deleteExpiredQueues();
    }
}