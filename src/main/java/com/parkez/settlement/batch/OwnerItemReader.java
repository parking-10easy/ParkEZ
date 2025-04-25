package com.parkez.settlement.batch;

import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@StepScope // 매 Job 마다 ownerItemReader 가 fresh 하게 생성
public class OwnerItemReader implements ItemReader<User> {

    private final UserReader userReader;
    private final YearMonth targetMonth;

    private static final int PAGE_SIZE = 10;

    private int currentPage = 0;
    private List<User> currentChunk = new ArrayList<>();
    private int currentIndex = 0;

    public OwnerItemReader(
            UserReader userReader,
            @Value("#{jobParameters['targetMonth']}") String targetMonthString
    ) {
        this.userReader = userReader;
        this.targetMonth = YearMonth.parse(targetMonthString);
    }

    @Override
    public User read() {
        log.info("current Page = {}, current Index = {}", currentPage, currentIndex);
        if (currentIndex >= currentChunk.size()) {
            // 다음 페이지 조회
            this.currentChunk = userReader.findOwnersForSettlementByMonth(targetMonth, currentPage++, PAGE_SIZE);

            // 더 이상 데이터가 없을 경우 종료
            if (currentChunk.isEmpty()) {
                return null;
            }
            currentIndex = 0;
        }
        return currentChunk.get(currentIndex++);
    }
}
