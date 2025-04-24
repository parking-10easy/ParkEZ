package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "진행 중인 프로모션 상세 응답")
public class PromotionDetailResponse {

	@Schema(description = "프로모션 ID", example = "1")
	private final Long id;

	@Schema(description = "프로모션 이름", example = "DAILY 2000")
	private final String promotionName;

	@Schema(description = "프로모션 타입 (예: DAILY, WELCOME)", example = "DAILY")
	private final PromotionType promotionType;

	@Schema(description = "프로모션 시작일", example = "2025-05-01T00:00:00")
	private final LocalDateTime promotionStartAt;

	@Schema(description = "프로모션 종료일", example = "2025-05-31T23:59:59")
	private final LocalDateTime promotionEndAt;

	@Schema(description = "발급일 기준 유효기간 (일)", example = "3")
	private final Integer validDaysAfterIssue;

	@Schema(description = "전체 발급 수량 제한", example = "1000")
	private final Integer limitTotal;

	@Schema(description = "한 유저당 최대 발급 수량", example = "1")
	private final Integer limitPerUser;

	@Schema(description = "쿠폰 이름", example = "신규가입 2000원 할인 쿠폰")
	private final String couponName;

	@Schema(description = "쿠폰 할인 금액 또는 비율", example = "2000")
	private final Integer discountValue;

	@Schema(description = "현재 유저 기준 발급 가능 여부", example = "true")
	private final boolean isAvailableToIssue;

	@Schema(description = "현재 남은 발급 수량", example = "721")
	private final Integer remainingQuantity;

	@Schema(description = "남은 발급 가능 횟수", example = "1")
	private final Integer availableIssueCount;

	@Schema(description = "현재 시점 기준 발급 시 만료일", example = "2025-05-04T00:00:00")
	private final LocalDateTime expiresAtIfIssuedNow;

	@Builder
	private PromotionDetailResponse(Long id, String promotionName, PromotionType promotionType,
		LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, Integer validDaysAfterIssue, Integer limitTotal,
		Integer limitPerUser, String couponName, Integer discountValue, boolean isAvailableToIssue,
		Integer remainingQuantity, Integer availableIssueCount, LocalDateTime expiresAtIfIssuedNow) {
		this.id = id;
		this.promotionName = promotionName;
		this.promotionType = promotionType;
		this.promotionStartAt = promotionStartAt;
		this.promotionEndAt = promotionEndAt;
		this.validDaysAfterIssue = validDaysAfterIssue;
		this.limitTotal = limitTotal;
		this.limitPerUser = limitPerUser;
		this.couponName = couponName;
		this.discountValue = discountValue;
		this.isAvailableToIssue = isAvailableToIssue;
		this.remainingQuantity = remainingQuantity;
		this.availableIssueCount = availableIssueCount;
		this.expiresAtIfIssuedNow = expiresAtIfIssuedNow;
	}

	public static PromotionDetailResponse from(PromotionDetail activePromotion) {
		return PromotionDetailResponse.builder()
			.id(activePromotion.getId())
			.promotionName(activePromotion.getPromotionName())
			.promotionType(activePromotion.getPromotionType())
			.promotionStartAt(activePromotion.getPromotionStartAt())
			.promotionEndAt(activePromotion.getPromotionEndAt())
			.validDaysAfterIssue(activePromotion.getValidDaysAfterIssue())
			.limitTotal(activePromotion.getLimitTotal())
			.limitPerUser(activePromotion.getLimitPerUser())
			.couponName(activePromotion.getCouponName())
			.discountValue(activePromotion.getDiscountValue())
			.isAvailableToIssue(activePromotion.getIsAvailableToIssue())
			.remainingQuantity(activePromotion.getRemainingQuantity())
			.availableIssueCount(activePromotion.getAvailableIssueCount())
			.expiresAtIfIssuedNow(activePromotion.getExpiresAtIfIssuedNow())
			.build();
	}

}
