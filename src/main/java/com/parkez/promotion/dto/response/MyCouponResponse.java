package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.DiscountType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MyCouponResponse {

	@Schema(description = "프로모션 ID", example = "1")
	private final Long promotionId;

	@Schema(description = "프로모션 이름", example = "DAILY 할인 이벤트")
	private final String promotionName;

	@Schema(description = "쿠폰 이름", example = "WELCOME 10%")
	private final String couponName;

	@Schema(description = "할인 값 (정률 또는 정액)", example = "10")
	private final Integer discountValue;

	@Schema(description = "할인 타입 (PERCENT 또는 FIXED)", example = "PERCENT")
	private final DiscountType discountType;

	@Schema(description = "쿠폰 발급 일시", example = "2025-04-25T10:00:00")
	private final LocalDateTime issuedAt;

	@Schema(description = "쿠폰 만료 일시", example = "2025-04-30T23:59:59")
	private final LocalDateTime expiresAt;

	@Schema(description = "쿠폰 사용 여부", example = "false")
	private final LocalDateTime usedAt;

	@Builder
	public MyCouponResponse(Long promotionId, String promotionName, String couponName, Integer discountValue,
		DiscountType discountType, LocalDateTime issuedAt, LocalDateTime expiresAt, LocalDateTime usedAt) {
		this.promotionId = promotionId;
		this.promotionName = promotionName;
		this.couponName = couponName;
		this.discountValue = discountValue;
		this.discountType = discountType;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.usedAt = usedAt;
	}
}
