package com.parkez.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    private static final String TARGET_MONTH = "targetMonth";

    // 매월 10일 새벽 1시에 실행
//    @Scheduled(cron = "0 0 1 10 * ?")
    @Scheduled(cron = "0 */1 * * * ?")
    @SchedulerLock(name = "settlementScheduler_batchProcess", lockAtLeastFor = "55s", lockAtMostFor = "10m")
    public void confirmMonthlySettlements() throws Exception {

        log.info("settlement schedule start");

        YearMonth targetMonth = YearMonth.now().minusMonths(1);
//        LocalDateTime now = LocalDateTime.now();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString(TARGET_MONTH, targetMonth.toString())
//                .addString(TARGET_MONTH, now.toString())
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("settlementJob"), jobParameters);
    }
}
