package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "프로모션 생성 응답")
public class PromotionCreateResponse {

	@Schema(description = "프로모션 아이디", example = "1")
	private final Long id;

	@Schema(description = "프로모션 이름", example = "DAILY 2000")
	private final String name;

	@Schema(description = "프로모션 타입 (DAILY)", example = "DAILY")
	private final PromotionType promotionType;

	@Schema(description = "프로모션에 연결된 쿠폰 정보")
	private final CouponResponse couponResponse;

	@Schema(description = "총 발급 가능 수량", example = "1000")
	private final Integer limitTotal;

	@Schema(description = "한 유저당 최대 발급 수량", example = "1")
	private final Integer limitPerUser;

	@Schema(description = "프로모션 시작 시각", example = "2025-05-01T00:00:00")
	private final LocalDateTime promotionStartAt;

	@Schema(description = "프로모션 종료 시각", example = "2025-05-31T23:59:59")
	private final LocalDateTime promotionEndAt;

	@Schema(description = "쿠폰 발급일 기준 유효기간(일)", example = "3")
	private final Integer validDaysAfterIssue;

	@Builder
	private PromotionCreateResponse(Long id, String name, PromotionType promotionType, CouponResponse couponResponse,
		Integer limitTotal, Integer limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		Integer validDaysAfterIssue) {
		this.id = id;
		this.name = name;
		this.promotionType = promotionType;
		this.couponResponse = couponResponse;
		this.limitTotal = limitTotal;
		this.limitPerUser = limitPerUser;
		this.promotionStartAt = promotionStartAt;
		this.promotionEndAt = promotionEndAt;
		this.validDaysAfterIssue = validDaysAfterIssue;
	}

	public static PromotionCreateResponse from(Promotion promotion) {
		return PromotionCreateResponse.builder()
			.id(promotion.getId())
			.name(promotion.getName())
			.promotionType(promotion.getPromotionType())
			.couponResponse(CouponResponse.from(promotion.getCoupon()))
			.limitTotal(promotion.getLimitTotal())
			.limitPerUser(promotion.getLimitPerUser())
			.promotionStartAt(promotion.getPromotionStartAt())
			.promotionEndAt(promotion.getPromotionEndAt())
			.validDaysAfterIssue(promotion.getValidDaysAfterIssue())
			.build();
	}

}
