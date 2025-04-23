package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "진행 중인 프로모션 리스트 응답")
public class ActivePromotionResponse {

	@Schema(description = "프로모션 ID", example = "1")
	private final Long id;

	@Schema(description = "프로모션 이름", example = "DAILY 2000")
	private final String promotionName;

	@Schema(description = "프로모션 타입", example = "DAILY")
	private final PromotionType promotionType;

	@Schema(description = "유저당 최대 발급 수량", example = "1")
	private final Integer limitPerUser;

	@Schema(description = "프로모션 시작일", example = "2025-05-01T00:00:00")
	private final LocalDateTime promotionStartAt;

	@Schema(description = "프로모션 종료일", example = "2025-05-31T23:59:59")
	private final LocalDateTime promotionEndAt;

	@Schema(description = "쿠폰 할인 금액 또는 할인율", example = "2000")
	private final Integer discountValue;

	@Schema(description = "쿠폰 이름", example = "신규가입 2000원 할인 쿠폰")
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
