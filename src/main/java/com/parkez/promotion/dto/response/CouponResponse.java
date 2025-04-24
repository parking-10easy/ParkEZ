package com.parkez.promotion.dto.response;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.enums.DiscountType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "쿠폰 생성 응답")
public class CouponResponse {

	@Schema(description = "쿠폰아이디", example = "1")
	private final Long id;

	@Schema(description = "쿠폰명", example = "신규가입 2000원 할인 쿠폰")
	private final String name;

	@Schema(description = "할인 타입 (FIXED: 고정금액, PERCENT: 비율)", example = "FIXED")
	private final DiscountType discountType;

	@Schema(description = "할인 금액 또는 비율", example = "2000")
	private final Integer discountValue;

	@Builder
	private CouponResponse(Long id, String name, DiscountType discountType, Integer discountValue) {
		this.id = id;
		this.name = name;
		this.discountType = discountType;
		this.discountValue = discountValue;
	}

	public static CouponResponse from(Coupon coupon) {
		return CouponResponse.builder()
			.id(coupon.getId())
			.name(coupon.getName())
			.discountType(coupon.getDiscountType())
			.discountValue(coupon.getDiscountValue())
			.build();
	}


}
