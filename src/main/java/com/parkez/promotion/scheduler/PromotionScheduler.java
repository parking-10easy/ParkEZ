package com.parkez.promotion.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.service.PromotionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PromotionScheduler {

	private final PromotionService promotionService;

	@Scheduled(cron = "0 */10 * * * *")
	public void expireEndedPromotionStatus() {

		LocalDateTime currentDateTime = LocalDateTime.now();
		PromotionStatus currentStatus = PromotionStatus.ACTIVE;
		PromotionStatus targetStatus = PromotionStatus.ENDED;

		int endedCount = promotionService.expireEndedPromotions(currentDateTime, currentStatus, targetStatus);

		log.info("[프로모션 상태 업데이트] 종료 처리된 프로모션: {}건", endedCount);
	}

	@Scheduled(cron = "0 */10 * * * *")
	public void expireSoldOutPromotionStatus() {

		PromotionStatus currentStatus = PromotionStatus.ACTIVE;
		PromotionStatus targetStatus = PromotionStatus.ENDED;

		int soldOutCount = promotionService.expireSoldOutPromotions(currentStatus, targetStatus);

		log.info("[조기 종료 프로모션 상태 업데이트] 조기 종료 처리된 프로모션: {}건", soldOutCount);
	}
}
