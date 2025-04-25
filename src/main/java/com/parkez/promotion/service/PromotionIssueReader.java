package com.parkez.promotion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.domain.repository.PromotionIssueRepository;
import com.parkez.promotion.dto.response.MyCouponResponse;
import com.parkez.promotion.excption.PromotionIssueErrorCode;

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

	public Page<MyCouponResponse> findMyIssuedCoupons(Long userId, PromotionIssueStatus status, Pageable pageable,
		PromotionIssueSortBy sortBy, Direction direction) {
			return promotionIssueRepository.findMyCouponsByStatus(userId, status, pageable, sortBy, direction);
	}

	public PromotionIssue getWithPromotionAndCouponById(Long promotionIssueId) {
		return promotionIssueRepository.findWithPromotionAndCouponById(promotionIssueId).orElseThrow(
			()-> new ParkingEasyException(PromotionIssueErrorCode.PROMOTION_ISSUE_NOT_FOUND)
		);
	}

}
