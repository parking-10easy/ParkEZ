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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final UserReader userReader;
    private final SettlementService settlementService;

    @Bean
    // 첫번째 매개변수 : 해당 Job을 지칭할 이름 선언 ("settlementJob")
    // 두번째 매개변수 : 해당 작업에 대한 트래킹을 진행하기 위해 jobRepository를 넣어주면
    //                스프링 배치가 자동으로 작업이 진행되는지를 메타 데이터 테이블에 기록해준다.
    public Job settlementJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep) // 이 작업에서 처음 시작할 Step 선언, Step이 1개 이상일 때, next()로 이후의 Step을 선언
                .build();
    }

    @Bean
    public Step settlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            ItemReader<User> settlementItemReader,
            ItemProcessor<User, User> settlementItemProcessor,
            ItemWriter<User> settlementItemWriter
    ) {
        // <[Reader에서 읽어들일 데이터 타입], [Writer에서 쓸 데이터 타입]>
        return new StepBuilder("settlementStep", jobRepository)
                .<User, User>chunk(10, platformTransactionManager) // chunkSize = 10 -> 10개 데이터 단위로 읽기, 처리, 쓰기 진행
                .reader(settlementItemReader) // 읽는 메서드 자리
                .processor(settlementItemProcessor) // 처리 메서드 자리
                .writer(settlementItemWriter) // 쓰기 메서드 자리
                .build();
    }

    @Bean
    @StepScope // 매 Job 마다 ownerItemReader 가 fresh 하게 생성
    public ItemReader<User> settlementItemReader() {
        return new OwnerItemReader(userReader);
    }

    @Bean
    @StepScope
    public ItemProcessor<User, User> settlementItemProcessor(
            @Value("#{jobParameters['runtime']}") String runtimeString
    ) {
        return owner -> {
            LocalDateTime runtime = LocalDateTime.parse(runtimeString);
            YearMonth targetMonth = YearMonth.from(runtime).minusMonths(1); // 직전 달 정산
            log.info("정산 수행 중 - ownerId={}, month={}", owner.getId(), targetMonth);

            try {
                settlementService.generateMonthlySettlement(owner, targetMonth); // 정산 로직 실행
                log.info("[정산 생성 성공] ownerId={}, month={}", owner.getId(), targetMonth);
            } catch (Exception e) {
                log.error("[정산 생성 실패] ownerId={}, month={}", owner.getId(), targetMonth, e);
            }
            return owner; // settlementService.generateMonthlySettlement(owner, targetMonth)를 통해 모든 정산 로직이 종료되지만
            //               ItemWriter 는 필수 구성 요소이므로 Writer 로 전달할 반환 값 필요
        };
    }

    // Writer 는 필수 구성 요소지만, 정산 처리 로직은 Processor 에서 완료되므로 no-operation 처리
    @Bean
    public ItemWriter<User> settlementItemWriter() {
        return item -> {};
    }
}
