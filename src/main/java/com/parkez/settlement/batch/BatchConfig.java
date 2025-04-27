package com.parkez.settlement.batch;

import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final String SETTLEMENT_JOB = "settlementJob";
    private static final String SETTLEMENT_STEP = "settlementStep";

    @Bean
    // 첫번째 매개변수 : 해당 Job 을 지칭할 이름 선언 ("settlementJob")
    // 두번째 매개변수 : 해당 작업에 대한 트래킹을 진행하기 위해 jobRepository 를 넣어주면
    //                스프링 배치가 자동으로 작업이 진행되는지를 메타 데이터 테이블에 기록해준다.
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder(SETTLEMENT_JOB, jobRepository)
                .start(settlementStep) // 이 작업에서 처음 시작할 Step 선언, Step 이 1개 이상일 때, next()로 이후의 Step 을 선언
                .build();
    }

    @Bean
    public Step settlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            ItemReader<User> ownerItemReader,
            ItemProcessor<User, SettlementBatchProcessResponse> settlementItemProcessor,
            ItemWriter<SettlementBatchProcessResponse> settlementItemWriter
    ) {
        // <[Reader 에서 읽어들일 데이터 타입], [Writer 에서 쓸 데이터 타입]>
        return new StepBuilder(SETTLEMENT_STEP, jobRepository)
                .<User, SettlementBatchProcessResponse>chunk(10, platformTransactionManager) // chunkSize = 10 -> 10개 데이터 단위로 읽기, 처리, 쓰기 진행
                .reader(ownerItemReader) // 읽는 메서드 자리
                .processor(settlementItemProcessor) // 처리 메서드 자리
                .writer(settlementItemWriter) // 쓰기 메서드 자리
                .build();
    }
}
