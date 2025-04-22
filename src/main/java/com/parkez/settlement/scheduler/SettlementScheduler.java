package com.parkez.settlement.scheduler;

import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final UserReader userReader;
    private final SettlementService settlementService;

    // 매월 10일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 10 * ?")
    public void confirmMonthlySettlements() {
        YearMonth targetMonth = YearMonth.now().minusMonths(1); // 직전 달 정산

        List<User> owners = userReader.findAllOwners(); // 사장님들 리스트

        for (User owner : owners) {
            try {
                settlementService.generateMonthlySettlement(owner, targetMonth);
                log.info("[정산 생성 성공] ownerId={}, month={}", owner.getId(), targetMonth);
            } catch (Exception e) {
                log.error("[정산 생성 실패] ownerId={}, month={}", owner.getId(), targetMonth, e);
            }
        }
    }

}
