package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ActivePromotionResponse {

	private final Long id;
	private final String promotionName;
	private final PromotionType promotionType;
	private final Integer limitPerUser;
	private final LocalDateTime promotionStartAt;
	private final LocalDateTime promotionEndAt;
	private final Integer discountValue;
	private final String couponName;

	@Builder
	private ActivePromotionResponse(Long id, String promotionName, PromotionType promotionType, Integer limitPerUser,
		LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, Integer discountValue, String couponName) {
		this.id = id;
		this.promotionName = promotionName;
		this.promotionType = promotionType;
		this.limitPerUser = limitPerUser;
		this.promotionStartAt = promotionStartAt;
		this.promotionEndAt = promotionEndAt;
		this.discountValue = discountValue;
		this.couponName = couponName;
	}

	public static ActivePromotionResponse from(ActivePromotionProjection activePromotions) {
		return ActivePromotionResponse.builder()
			.id(activePromotions.getId())
			.promotionName(activePromotions.getPromotionName())
			.promotionType(activePromotions.getPromotionType())
			.limitPerUser(activePromotions.getLimitPerUser())
			.promotionStartAt(activePromotions.getPromotionStartAt())
			.promotionEndAt(activePromotions.getPromotionEndAt())
			.discountValue(activePromotions.getDiscountValue())
			.couponName(activePromotions.getCouponName())
			.build();
	}
}
