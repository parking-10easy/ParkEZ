package com.parkez.settlement.batch;

import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.YearMonth;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final UserReader userReader;
    private final SettlementService settlementService;

    @Bean
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Step settlementStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("settlementStep", jobRepository)
                .<User, User>chunk(10, platformTransactionManager)
                .reader(settlementItemReader())
                .processor(settlementItemProcessor())
                .writer(settlementItemWriter())
                .build();
    }

    @Bean
    @StepScope // 매 Job 마다 ownerItemReader 가 fresh 하게 생성
    public ItemReader<User> settlementItemReader() {
        return new OwnerItemReader(userReader);
    }

    @Bean
    public ItemProcessor<User, User> settlementItemProcessor() {
        return owner -> {
            YearMonth targetMonth = YearMonth.now().minusMonths(1); // 직전 달 정산
            log.info("정산 수행 중 - ownerId={}, month={}", owner.getId(), targetMonth);

            try {
                settlementService.generateMonthlySettlement(owner, targetMonth);
                log.info("[정산 생성 성공] ownerId={}, month={}", owner.getId(), targetMonth);
            } catch (Exception e) {
                log.error("[정산 생성 실패] ownerId={}, month={}", owner.getId(), targetMonth, e);
            }
            return owner;
        };
    }

    // Writer 는 필수 구성 요소지만, 정산 처리 로직은 Processor 에서 완료되므로 no-operation 처리
    @Bean
    public ItemWriter<User> settlementItemWriter() {
        return item -> {};
    }
}
