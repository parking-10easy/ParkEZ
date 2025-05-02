package com.parkez.settlement.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private JobRegistry jobRegistry;
    @Mock
    private Job settlementJob;
    @Mock
    private JobExecution jobExecution;
    @InjectMocks
    private SettlementScheduler settlementScheduler;

    @Test
    void settlementJob_정상적으로_실행_테스트() throws Exception {
        // given
        given(jobRegistry.getJob(anyString())).willReturn(settlementJob);
        given(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .willReturn(jobExecution);

        // when
        settlementScheduler.confirmMonthlySettlements();

        // then
        verify(jobRegistry).getJob("settlementJob");
        verify(jobLauncher).run(eq(settlementJob), any(JobParameters.class));
    }
}