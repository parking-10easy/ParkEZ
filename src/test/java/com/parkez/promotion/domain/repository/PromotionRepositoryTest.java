package com.parkez.promotion.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.common.config.PersistenceConfig;
import com.parkez.common.config.QueryDslConfig;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, PersistenceConfig.class})
class PromotionRepositoryTest {

	@Autowired
	private PromotionRepository promotionRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Nested
	class FindActivePromotions {

		@Test
		public void 종료된_포로모션은_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now;
			LocalDateTime promotionEndAt = now.plusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);

			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			promotion.updateStatus(PromotionStatus.ENDED);
			promotionRepository.save(promotion);

			//when
			Page<ActivePromotionProjection> activePromotions = promotionRepository.findActivePromotions(now,
				PromotionStatus.ACTIVE, PageRequest.of(0, 10));

			//then
			assertThat(activePromotions).isEmpty();

		}

		@Test
		public void 프로모션_시작전이면_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now.plusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(2);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);

			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			promotionRepository.save(promotion);

			//when
			Page<ActivePromotionProjection> activePromotions = promotionRepository.findActivePromotions(now,
				PromotionStatus.ACTIVE, PageRequest.of(0, 10));

			//then
			assertThat(activePromotions).isEmpty();

		}

		@Test
		public void 프로모션_기간이_자니면_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now.minusDays(3);
			LocalDateTime promotionEndAt = now.minusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);

			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			promotionRepository.save(promotion);

			//when
			Page<ActivePromotionProjection> activePromotions = promotionRepository.findActivePromotions(now,
				PromotionStatus.ACTIVE, PageRequest.of(0, 10));

			//then
			assertThat(activePromotions).isEmpty();

		}

		@Test
		public void 진행중인_프로모션_조회_없으면_빈배열_반환() {
			//given
			LocalDateTime now = LocalDateTime.now();
			PromotionStatus promotionStatus = PromotionStatus.ACTIVE;
			Pageable pageable = PageRequest.of(0, 10);

			//when
			Page<ActivePromotionProjection> activePromotions = promotionRepository.findActivePromotions(now,
				promotionStatus, pageable);

			//then
			assertThat(activePromotions).isEmpty();

		}

		@Test
		public void 진행중인_프로모션_한건_정상적으로_조회할_수_있다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now;
			LocalDateTime promotionEndAt = now.plusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);

			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			promotionRepository.save(promotion);

			//when
			Page<ActivePromotionProjection> activePromotions = promotionRepository.findActivePromotions(now,
				PromotionStatus.ACTIVE, PageRequest.of(0, 10));

			//then
			assertThat(activePromotions).size().isEqualTo(1);
			assertThat(activePromotions.getContent())
				.extracting(
					"id","promotionName", "promotionType", "limitPerUser", "promotionStartAt", "promotionEndAt", "discountValue", "couponName"
				).containsExactly(
					Tuple.tuple(1L, promotionName, promotionType,limitPerUser,promotionStartAt, promotionEndAt, discountValue,couponName)
				);

		}
	}

	private Promotion createPromotion(String promotionName, PromotionType promotionType, Coupon coupon, int limitTotal,
		int limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, int validDaysAfterIssue) {
		return Promotion.builder()
			.name(promotionName)
			.promotionType(promotionType)
			.coupon(coupon)
			.limitTotal(limitTotal)
			.limitPerUser(limitPerUser)
			.promotionStartAt(promotionStartAt)
			.promotionEndAt(promotionEndAt)
			.validDaysAfterIssue(validDaysAfterIssue)
			.build();
	}

	private Coupon createCoupon(String name, DiscountType discountType, int discountValue, String description) {
		return Coupon.builder()
			.name(name)
			.discountType(discountType)
			.discountValue(discountValue)
			.description(description)
			.build();
	}

}