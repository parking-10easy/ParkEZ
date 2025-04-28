package com.parkez.promotion.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.service.PromotionIssueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionIssueScheduler {

	private final PromotionIssueService promotionIssueService;

	@Scheduled(cron = "0 */10 * * * *")
	public void expirePromotionIssues() {

		LocalDateTime currentDateTime = LocalDateTime.now();
		PromotionIssueStatus currentStatus = PromotionIssueStatus.ISSUED;
		PromotionIssueStatus targetStatus = PromotionIssueStatus.EXPIRED;

		int expiredPromotionIssuesCount = promotionIssueService.expirePromotionIssues(currentDateTime, currentStatus, targetStatus);

		log.info("[쿠폰 만료처리] {}건의 발급된 쿠폰을 만료 처리했습니다. (기준 시간: {})", expiredPromotionIssuesCount, currentDateTime);
	}
}
