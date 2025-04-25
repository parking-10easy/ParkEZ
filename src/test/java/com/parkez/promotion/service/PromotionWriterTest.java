package com.parkez.promotion.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.common.config.PersistenceConfig;
import com.parkez.common.config.QueryDslConfig;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.CouponRepository;
import com.parkez.promotion.domain.repository.PromotionIssueRepository;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, PersistenceConfig.class})
class PromotionWriterTest {

	@Autowired
	private PromotionRepository promotionRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PromotionIssueRepository promotionIssueRepository;

	private PromotionWriter promotionWriter;

	@BeforeEach
	void setUp() {
		this.promotionWriter = new PromotionWriter(promotionRepository);
	}

	@Nested
	class Create {

		@Test
		public void 프로모션_생성_유효한_값으로_프로모션을_생성하고_저장한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			int validDaysAfterIssue = 3;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);

			//when
			Promotion savedPromotion = promotionWriter.create(savedCoupon, promotionName, promotionType, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);

			//then
			Assertions.assertThat(savedPromotion)
				.extracting(
					"name", "promotionType", "coupon.id", "coupon.name", "limitTotal", "limitPerUser",
					"promotionStartAt", "promotionEndAt", "validDaysAfterIssue"
				)
				.containsExactly(
					promotionName, promotionType, coupon.getId(), coupon.getName(),
					limitTotal, limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue
				);

		}
	}

	@Nested
	class ExpireEndedPromotions {

		@Test
		public void 종료된_프로모션의_상태를_ENDED_로_변경할_수있다() {
			//given
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);

			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.of(2025, 4, 1, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime currentDateTime = LocalDateTime.of(2025, 4, 10, 10, 0);
			PromotionStatus currentStatus = PromotionStatus.ACTIVE;
			PromotionStatus targetStatus = PromotionStatus.ENDED;

			//when
			int endedPromotionCount = promotionWriter.expireEndedPromotions(currentDateTime, currentStatus, targetStatus);

			entityManager.flush();
			entityManager.clear();

			//then
			assertThat(endedPromotionCount).isEqualTo(1);
			Promotion target = promotionRepository.findById(savedPromotion.getId()).get();
			Assertions.assertThat(target.getPromotionStatus()).isEqualTo(PromotionStatus.ENDED);

		}
	}

	@Nested
	class ExpireSoldOutPromotionStatus {

		@Test
		public void 쿠폰_매진된_프로모션의_상태를_ENDED_로_변경할_수있다() {
			//given
			User user = createUser();
			User savedUser = userRepository.save(user);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);

			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = now;

			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			promotionIssueRepository.save(promotionIssue);

			PromotionStatus currentStatus = PromotionStatus.ACTIVE;
			PromotionStatus targetStatus = PromotionStatus.ENDED;

			//when
			int endedPromotionCount = promotionWriter.expireSoldOutPromotions(currentStatus, targetStatus);

			entityManager.flush();
			entityManager.clear();

			//then
			assertThat(endedPromotionCount).isEqualTo(1);
			Promotion target = promotionRepository.findById(savedPromotion.getId()).get();
			Assertions.assertThat(target.getPromotionStatus()).isEqualTo(PromotionStatus.ENDED);

		}
	}

	private Coupon createCoupon(String name, DiscountType discountType, int discountValue, String description) {
		return Coupon.builder()
			.name(name)
			.discountType(discountType)
			.discountValue(discountValue)
			.description(description)
			.build();
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

	private User createUser() {
		return User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg");
	}

	private PromotionIssue createPromotionIssue(Promotion savedPromotion, User savedUser, LocalDateTime issuedAt,
		Promotion promotion) {
		return PromotionIssue.builder()
			.promotion(savedPromotion)
			.user(savedUser)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plusDays(promotion.getValidDaysAfterIssue()))
			.build();
	}

}