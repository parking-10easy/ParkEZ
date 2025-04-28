package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PromotionIssueResponse {

	@Schema(description = "발급된 프로모션 ID", example = "1")
	private final Long promotionId;

	@Schema(description = "발급된 쿠폰 ID", example = "1")
	private final Long couponId;

	@Schema(description = "쿠폰 이름", example = "WELCOME 10%")
	private final String couponName;

	@Schema(description = "쿠폰 발급 일시", example = "2025-04-25T10:00:00")
	private final LocalDateTime issuedAt;

	@Schema(description = "쿠폰 만료 일시", example = "2025-04-30T23:59:59")
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
