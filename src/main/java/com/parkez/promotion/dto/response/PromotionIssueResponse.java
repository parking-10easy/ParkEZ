package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PromotionIssueResponse {

	private final Long promotionId;
	private final Long couponId;
	private final String couponName;
	private final LocalDateTime issuedAt;
	private final LocalDateTime expiresAt;

	@Builder
	private PromotionIssueResponse(Long promotionId, Long couponId, String couponName, LocalDateTime issuedAt,
		LocalDateTime expiresAt) {
		this.promotionId = promotionId;
		this.couponId = couponId;
		this.couponName = couponName;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
	}

	public static PromotionIssueResponse of(Promotion promotion, PromotionIssue promotionIssue) {
		return PromotionIssueResponse.builder()
			.promotionId(promotion.getId())
			.couponId(promotion.getCouponId())
			.couponName(promotion.getCouponName())
			.issuedAt(promotionIssue.getIssuedAt())
			.expiresAt(promotionIssue.getExpiresAt())
			.build();
	}

}
