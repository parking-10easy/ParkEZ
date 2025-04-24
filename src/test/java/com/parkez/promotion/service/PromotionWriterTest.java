package com.parkez.promotion.service;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class PromotionWriterTest {

	@Mock
	private PromotionRepository promotionRepository;

	@InjectMocks
	private PromotionWriter promotionWriter;

	@Nested
	class Create {

		@Test
		public void 프로모션_생성_유효한_값으로_프로모션을_생성하고_저장한다() {
			//given
			long promotionId = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			long couponId = 1L;
			int validDaysAfterIssue = 3;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponId, couponName, discountType, discountValue, description);

			Promotion promotion = createPromotion(promotionId, promotionName, promotionType, coupon, limitTotal, limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue, PromotionStatus.ACTIVE);

			given(promotionRepository.save(any(Promotion.class))).willReturn(promotion);

			//when
			Promotion result = promotionWriter.create(coupon, promotionName, promotionType, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);

			//then
			Assertions.assertThat(result)
				.extracting(
					"id", "name", "promotionType", "coupon.id", "coupon.name",
					"coupon.discountType", "coupon.discountValue", "limitTotal", "limitPerUser",
					"promotionStartAt", "promotionEndAt", "validDaysAfterIssue"
				)
				.containsExactly(
					promotionId, promotionName, promotionType, couponId, couponName, discountType, discountValue,
					limitTotal, limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue
				);

		}
	}

	private Coupon createCoupon(Long id, String name, DiscountType discountType, int discountValue,
		String description) {
		Coupon coupon = Coupon.builder()
			.name(name)
			.discountType(discountType)
			.discountValue(discountValue)
			.description(description)
			.build();
		ReflectionTestUtils.setField(coupon, "id", id);
		return coupon;
	}

	private Promotion createPromotion(Long promotionId, String promotionName, PromotionType promotionType,
		Coupon coupon, int limitTotal,
		int limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, int validDaysAfterIssue,
		PromotionStatus promotionStatus) {
		Promotion promotion = Promotion.builder()
			.name(promotionName)
			.promotionType(promotionType)
			.coupon(coupon)
			.limitTotal(limitTotal)
			.limitPerUser(limitPerUser)
			.promotionStartAt(promotionStartAt)
			.promotionEndAt(promotionEndAt)
			.validDaysAfterIssue(validDaysAfterIssue)
			.build();
		ReflectionTestUtils.setField(promotion, "id", promotionId);
		ReflectionTestUtils.setField(promotion, "promotionStatus", promotionStatus);
		return promotion;
	}

}