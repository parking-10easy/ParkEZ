package com.parkez.promotion.domain.repository.projection;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;

public interface ActivePromotionProjection {

	Long getId();
	String getPromotionName();
	PromotionType getPromotionType();
	Integer getLimitPerUser();
	LocalDateTime getPromotionStartAt();
	LocalDateTime getPromotionEndAt();
	Integer getDiscountValue();
	String getCouponName();

}
