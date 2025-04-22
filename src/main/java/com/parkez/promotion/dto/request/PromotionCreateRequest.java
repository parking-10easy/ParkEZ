package com.parkez.promotion.dto.request;

import java.time.LocalDateTime;

import com.parkez.promotion.domain.enums.PromotionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "프로모션 생성 요청")
public class PromotionCreateRequest {

	@NotBlank(message = "프로모션 명은 필수 값입니다.")
	@Schema(description = "프로모션 이름", example = "DAILY 2000")
	private String name;

	@NotBlank(message = "프로모션 타입은 필수 값입니다.")
	@Schema(description = "프로모션 타입 (DAILY)", example = "DAILY")
	private PromotionType promotionType;

	@NotNull(message = "쿠폰 아이디는 필수 값입니다.")
	@Schema(description = "연결할 쿠폰 ID", example = "1")
	private Long couponId;

	@NotNull(message = "총 발급 수량 제한은 필수입니다.")
	@Min(value = 1, message = "최소 1개 이상이어야 합니다.")
	@Schema(description = "총 발급 가능 수량", example = "1000")
	private Integer limitTotal;

	@NotNull(message = "유저당 발급 수량 제한은 필수입니다.")
	@Min(value = 1, message = "최소 1개 이상이어야 합니다.")
	@Schema(description = "한 유저당 최대 발급 수량", example = "1")
	private Integer limitPerUser;

	@NotNull(message = "프로모션 시작일은 필수 값입니다.")
	@Schema(description = "프로모션 시작 시각", example = "2025-05-01T00:00:00")
	private LocalDateTime promotionStartAt;

	@NotNull(message = "프로모션 종료일은 필수 값입니다.")
	@Schema(description = "프로모션 종료 시각", example = "2025-05-31T23:59:59")
	private LocalDateTime promotionEndAt;

	@NotNull(message = "쿠폰 유효기간(일)은 필수 값입니다.")
	@Min(value = 1, message = "유효기간은 최소 1일 이상이어야 합니다.")
	@Schema(description = "쿠폰 발급일 기준 유효기간(일)", example = "3")
	private Integer validDaysAfterIssue;

	public PromotionCreateRequest() {
	}

	@Builder
	private PromotionCreateRequest(String name, PromotionType promotionType, Long couponId, Integer limitTotal,
		Integer limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		Integer validDaysAfterIssue) {
		this.name = name;
		this.promotionType = promotionType;
		this.couponId = couponId;
		this.limitTotal = limitTotal;
		this.limitPerUser = limitPerUser;
		this.promotionStartAt = promotionStartAt;
		this.promotionEndAt = promotionEndAt;
		this.validDaysAfterIssue = validDaysAfterIssue;
	}
}
