package com.parkez.promotion.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.dto.response.MyCouponResponse;

public interface PromotionQueryDslRepository {

	Page<MyCouponResponse> findMyCouponsByStatus(Long userId, PromotionIssueStatus status, Pageable pageable,
		PromotionIssueSortBy sortBy, Sort.Direction direction);
}
