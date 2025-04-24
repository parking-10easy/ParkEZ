package com.parkez.promotion.dto.request;

import com.parkez.promotion.domain.enums.DiscountType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "쿠폰 생성 요청")
public class CouponCreateRequest {

	@Schema(description = "쿠폰명", example = "신규가입 2000원 할인 쿠폰", required = true)
	@NotBlank(message = "쿠폰명은 필수 값입니다.")
	private String name;

	@Schema(description = "할인 타입 (FIXED: 고정금액, PERCENT: 비율)", example = "FIXED", required = true)
	@NotNull(message = "할인 타입은 필수 값입니다.")
	private DiscountType discountType;

	@Schema(description = "할인 금액 또는 비율", example = "2000", required = true)
	@NotNull(message = "할인 금액 또는 비율은 필수 값입니다.")
	@Min(value = 1, message = "할인 금액 또는 비율은 1 이상이어야 합니다.")
	private Integer discountValue;

	@Schema(description = "쿠폰 설명", example = "신규 유저 전용, 1회만 사용 가능")
	private String description;

	public CouponCreateRequest() {
	}

	@Builder
	private CouponCreateRequest(String name, DiscountType discountType, Integer discountValue, String description) {
		this.name = name;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.description = description;
	}
}
