package com.parkez.settlement.batch;

import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Component
@StepScope
public class SettlementItemProcessor implements ItemProcessor<User, SettlementBatchProcessResponse> {

    private final SettlementService settlementService;
    private final YearMonth targetMonth;

    public SettlementItemProcessor(
            SettlementService settlementService,
            @Value("#{jobParameters['targetMonth']}") String targetMonthString
    ) {
        this.settlementService = settlementService;
        this.targetMonth = YearMonth.parse(targetMonthString);
//        LocalDateTime now = LocalDateTime.parse(targetMonthString);
//        this.targetMonth = YearMonth.from(now).minusMonths(1);
    }

    @Override
    public SettlementBatchProcessResponse process(User owner) {
        log.info("[정산 수행 중] ownerId={}, month={}", owner.getId(), targetMonth);

        try {
            LocalDateTime settledAt = LocalDateTime.now();
            SettlementBatchProcessResponse response = SettlementBatchProcessResponse.success(
                    settlementService.generateMonthlySettlement(owner, targetMonth, settledAt).getSettlement(),
                    settlementService.generateMonthlySettlement(owner, targetMonth, settledAt).getSettlementDetails()
            ); // 정산 로직 실행
            log.info("[정산 생성 성공] ownerId={}, month={}", owner.getId(), targetMonth);
            return response;

        } catch (Exception e) {
            log.error("[정산 생성 실패] ownerId={}, month={}", owner.getId(), targetMonth, e);
            return SettlementBatchProcessResponse.failure(e.getMessage());
        }
    }
}
