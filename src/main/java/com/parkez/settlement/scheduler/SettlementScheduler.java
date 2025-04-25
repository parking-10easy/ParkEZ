package com.parkez.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    // 매월 10일 새벽 1시에 실행
    @Scheduled(cron = "0 */1 * * * ?")
    public void confirmMonthlySettlements() throws Exception {

        log.info("settlement schedule start");

        YearMonth targetMonth = YearMonth.now().minusMonths(1);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetMonth", targetMonth.toString())
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("settlementJob"), jobParameters);
    }
}
