package com.parkez.promotion.dto.request;

import org.springframework.data.domain.Sort.Direction;

import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyCouponSearchCondition {

	@Schema(description = "쿠폰 상태 (ISSUED, USED, EXPIRED)", example = "ISSUED")
	private PromotionIssueStatus status = PromotionIssueStatus.ISSUED;

	@Schema(description = "정렬 기준 (ISSUED_AT, EXPIRES_AT)", example = "ISSUED_AT")
	private PromotionIssueSortBy sortBy = PromotionIssueSortBy.ISSUED_AT;

	@Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC")
	private Direction direction = Direction.DESC;

	@Builder
	private MyCouponSearchCondition(PromotionIssueStatus status, PromotionIssueSortBy sortBy, Direction direction) {
		this.status = status;
		this.sortBy = sortBy;
		this.direction = direction;
	}
}
