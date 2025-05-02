package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.dto.request.MyCouponSearchCondition;
import com.parkez.promotion.dto.response.MyCouponResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionIssueService {

	private final PromotionIssueReader promotionIssueReader;
	private final PromotionIssueWriter promotionIssueWriter;

	public Page<MyCouponResponse> getMyIssuedCoupons(Long userId, MyCouponSearchCondition condition, int page,  int size) {

		Pageable pageable = PageRequest.of(page-1, size);

		return promotionIssueReader.findMyIssuedCoupons(userId, condition.getStatus(), pageable, condition.getSortBy(),condition.getDirection());
	}


	@Transactional
	public int expirePromotionIssues(LocalDateTime currentDateTime, PromotionIssueStatus currentStatus, PromotionIssueStatus targetStatus) {
		return promotionIssueWriter.expirePromotionIssues(currentDateTime, currentStatus, targetStatus);
	}

}
