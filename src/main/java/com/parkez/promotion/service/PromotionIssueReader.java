package com.parkez.promotion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.promotion.domain.repository.PromotionIssueRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionIssueReader {

	private final PromotionIssueRepository promotionIssueRepository;

	public int countByPromotionId(Long promotionId) {

		return promotionIssueRepository.countByPromotionId(promotionId);
	}

	public int countByPromotionIdAndUserId(Long promotionId, Long userId) {
		return promotionIssueRepository.countByPromotionIdAndUserId(promotionId, userId);
	}

}
