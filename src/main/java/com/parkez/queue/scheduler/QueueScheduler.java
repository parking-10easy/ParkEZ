package com.parkez.queue.scheduler;

import com.parkez.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueService queueService;

    @Scheduled(fixedDelay = 60_000) // 1분마다 실행
    public void deleteQueues() {
        queueService.deleteExpiredQueues();
    }


}
