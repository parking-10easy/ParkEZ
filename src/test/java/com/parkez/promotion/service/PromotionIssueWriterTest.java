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
class PromotionIssueWriterTest {

	@Autowired
	private PromotionIssueRepository promotionIssueRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PromotionRepository promotionRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private EntityManager entityManager;

	private PromotionIssueWriter promotionIssueWriter;

	@BeforeEach
	void setUp() {
		promotionIssueWriter = new PromotionIssueWriter(promotionIssueRepository);
	}

	@Nested
	class Create {

		@Test
		public void 프로모션_발급이력을_생성하고_저장할_수_있다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			User user = User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg");
			User savedUser = userRepository.save(user);
			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			//when
			PromotionIssue promotionIssue = promotionIssueWriter.create(savedPromotion, savedUser);

			//then
			Assertions.assertThat(promotionIssue)
				.extracting("promotion.id", "user.id", "expiresAt")
				.containsExactly(savedPromotion.getId(), savedUser.getId(), promotionIssue.getIssuedAt().plusDays(3));

		}
	}

	@Nested
	class BulkUpdateStatusByCurrentTime {

		@Test
		public void 만료시간이_지난_ISSUED_쿠폰은_EXPIRED_상태로_변경된다() {
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
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.of(2025, 4, 1, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = now;
			LocalDateTime expiresAt = issuedAt.plusDays(savedPromotion.getValidDaysAfterIssue());
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, expiresAt);
			promotionIssueRepository.save(promotionIssue);

			LocalDateTime currentDateTime = LocalDateTime.of(2025, 4, 10, 10, 0);
			PromotionIssueStatus currentStatus = PromotionIssueStatus.ISSUED;
			PromotionIssueStatus targetStatus = PromotionIssueStatus.EXPIRED;

			//when
			int expiredPromotionIssuesCount = promotionIssueWriter.expirePromotionIssues(currentDateTime, currentStatus, targetStatus);

			entityManager.flush();
			entityManager.clear();

			//then
			assertThat(expiredPromotionIssuesCount).isEqualTo(1);
			PromotionIssue target = promotionIssueRepository.findById(promotionIssue.getId()).get();
			Assertions.assertThat(target.getStatus()).isEqualTo(PromotionIssueStatus.EXPIRED);

		}
	}

	@Nested
	class Use {

		@Test
		public void 프로모션_쿠폰을_사용하면_used_상태로_변경된다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			User user = User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg");
			User savedUser = userRepository.save(user);
			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);
			LocalDateTime issuedAt = now;
			LocalDateTime expiresAt = issuedAt.plusDays(savedPromotion.getValidDaysAfterIssue());
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion,savedUser,issuedAt,expiresAt);

			//when
			promotionIssueWriter.use(promotionIssue, now);

			//then
			assertThat(promotionIssue).extracting(
				"usedAt", "status"
			).containsExactly(
				now, PromotionIssueStatus.USED
			);

		}
	}

	@Nested
	class CancelUsage {

		@Test
		public void 프로모션_쿠폰_사용_취소하면_ISSUED_상태로_변경되고_usedAt이_null된다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			User user = User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg");
			User savedUser = userRepository.save(user);
			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);
			LocalDateTime issuedAt = now;
			LocalDateTime expiresAt = issuedAt.plusDays(savedPromotion.getValidDaysAfterIssue());
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion,savedUser,issuedAt,expiresAt);

			//when
			promotionIssueWriter.cancelUsage(promotionIssue);

			//then
			assertThat(promotionIssue).extracting(
				"usedAt", "status"
			).containsExactly(
				null, PromotionIssueStatus.ISSUED
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

	private User createUser() {
		return User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg");
	}

	private PromotionIssue createPromotionIssue(Promotion savedPromotion, User savedUser, LocalDateTime issuedAt,
		LocalDateTime expiresAt) {
		return PromotionIssue.builder()
			.promotion(savedPromotion)
			.user(savedUser)
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.build();
	}
}