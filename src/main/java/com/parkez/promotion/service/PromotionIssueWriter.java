package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.domain.repository.PromotionIssueRepository;
import com.parkez.user.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PromotionIssueWriter {

	private final PromotionIssueRepository promotionIssueRepository;

	public PromotionIssue create(Promotion promotion, User user) {

		LocalDateTime issuedAt = LocalDateTime.now();
		LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());

		PromotionIssue promotionIssue = PromotionIssue.builder()
			.promotion(promotion)
			.user(user)
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.build();

		return promotionIssueRepository.save(promotionIssue);

	}

	public int expirePromotionIssues(LocalDateTime currentDateTime, PromotionIssueStatus currentStatus, PromotionIssueStatus targetStatus) {

		return promotionIssueRepository.bulkUpdateStatusByCurrentTime(currentDateTime, currentStatus, targetStatus);
	}

}
