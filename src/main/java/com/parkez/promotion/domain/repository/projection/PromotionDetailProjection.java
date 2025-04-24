package com.parkez.promotion.domain.repository.projection;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;

public interface PromotionDetailProjection {
	Long getId();

	String getPromotionName();

	PromotionType getPromotionType();

	LocalDateTime getPromotionStartAt();

	LocalDateTime getPromotionEndAt();

	Integer getValidDaysAfterIssue();

	Integer getLimitTotal();

	Integer getLimitPerUser();

	String getCouponName();

	Integer getDiscountValue();

	Boolean getIsAvailableToIssue();

	Integer getRemainingQuantity();

	Integer getAvailableIssueCount();

	LocalDateTime getExpiresAtIfIssuedNow();

}
