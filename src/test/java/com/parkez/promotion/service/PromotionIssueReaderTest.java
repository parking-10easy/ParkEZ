package com.parkez.promotion.service;

import static com.parkez.promotion.excption.PromotionIssueErrorCode.*;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.common.config.PersistenceConfig;
import com.parkez.common.config.QueryDslConfig;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.CouponRepository;
import com.parkez.promotion.domain.repository.PromotionIssueRepository;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.dto.response.MyCouponResponse;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, PersistenceConfig.class})
class PromotionIssueReaderTest {

	@Autowired
	private PromotionIssueRepository promotionIssueRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PromotionRepository promotionRepository;

	@Autowired
	private CouponRepository couponRepository;

	private PromotionIssueReader promotionIssueReader;

	@BeforeEach
	void setUp() {
		promotionIssueReader = new PromotionIssueReader(promotionIssueRepository);
	}

	@Nested
	class CountByPromotionId {

		@Test
		public void 특정_프로모션의_발급_된_쿠폰_개수를_정상적으로_조회할_수_있다() {
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

			User user = createUser();
			User savedUser = userRepository.save(user);
			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);
			LocalDateTime issuedAt = LocalDateTime.now();
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			promotionIssueRepository.save(promotionIssue);

			//when
			int issuedCount = promotionIssueReader.countByPromotionId(promotion.getId());

			//then
			Assertions.assertThat(issuedCount).isEqualTo(1);

		}
		
	}
	
	@Nested
	class CountByPromotionIdAndUserId{
		
		@Test
		public void 특정_유저가_특정_프로모션에서_발급한_쿠폰_개수를_정상적으로_조회한다() {
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

			User user = createUser();
			User savedUser = userRepository.save(user);
			Coupon coupon = createCoupon(couponName, discountType, discountValue, description);
			Coupon savedCoupon = couponRepository.save(coupon);
			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);
			LocalDateTime issuedAt = LocalDateTime.now();
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			promotionIssueRepository.save(promotionIssue);

			//when
			int issuedCount = promotionIssueReader.countByPromotionIdAndUserId(user.getId(),promotion.getId());

			//then
			Assertions.assertThat(issuedCount).isEqualTo(1);

		}
	}

	@Nested
	class FindMyIssuedCoupons {

		@Test
		public void 특정_유저의_발급된_쿠폰을_정상적으로_조회할_수_있다() {
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
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = now;

			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			promotionIssueRepository.save(promotionIssue);

			//when
			Page<MyCouponResponse> myIssuedCoupons = promotionIssueReader.findMyIssuedCoupons(user.getId(),
				PromotionIssueStatus.ISSUED, PageRequest.of(0, 10),
				PromotionIssueSortBy.ISSUED_AT, Sort.Direction.DESC);

			//then
			Assertions.assertThat(myIssuedCoupons.getContent()).hasSize(1);
			Assertions.assertThat(myIssuedCoupons.getContent())
				.extracting(
					"promotionId", "promotionName", "couponName", "discountValue", "discountType", "issuedAt", "expiresAt", "usedAt"
				).containsExactly(
					Tuple.tuple(promotionIssue.getId(), promotion.getName(), coupon.getName(), coupon.getDiscountValue(), coupon.getDiscountType(), promotionIssue.getIssuedAt(), promotionIssue.getExpiresAt(), promotionIssue.getUsedAt())
				);

		}
	}

	@Nested
	class GetWithPromotionAndCouponById {

		@Test
		public void 존재하지_않는_프로모션_발급을_조회하면_PROMOTION_ISSUE_NOT_FOUND_예외가_발생합니다() {
			//given
			Long promotionId = -1L;

			//when & then
			Assertions.assertThatThrownBy(()->promotionIssueReader.getWithPromotionAndCouponById(promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_ISSUE_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 프로모션쿠폰_아이디로_프로모션_쿠폰_정보를_함께_조회할_수있다() {
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
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = now;

			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			PromotionIssue savedPromotionIssue = promotionIssueRepository.save(promotionIssue);

			//when
			PromotionIssue result = promotionIssueReader.getWithPromotionAndCouponById(savedPromotionIssue.getId());

			//then
			Assertions.assertThat(result)
				.extracting(
					"promotion.name", "issuedAt", "promotion.coupon.name"
				).containsExactly(
					promotionName, issuedAt, couponName
				);

		}
	}

	@Nested
	class GetById {

		@Test
		public void 존재하지_않는_프로모션_발급을_조회하면_PROMOTION_ISSUE_NOT_FOUND_예외가_발생합니다() {
			//given
			Long promotionId = -1L;

			//when & then
			Assertions.assertThatThrownBy(()->promotionIssueReader.getById(promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_ISSUE_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 프로모션발급_아이디로_조회할_수_있다() {
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
			LocalDateTime now = LocalDateTime.of(2025, 4, 23, 10, 0);
			LocalDateTime promotionStartAt = now.minusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(1);

			Promotion promotion = createPromotion(promotionName, promotionType, savedCoupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = now;

			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion);
			PromotionIssue savedPromotionIssue = promotionIssueRepository.save(promotionIssue);

			//when
			PromotionIssue result = promotionIssueReader.getById(savedPromotionIssue.getId());

			//then
			Assertions.assertThat(result)
				.extracting(
					"issuedAt", "expiresAt","usedAt", "status"
				).containsExactly(
					issuedAt, issuedAt.plusDays(validDaysAfterIssue), null, PromotionIssueStatus.ISSUED
				);

		}
	}

	private static User createUser() {
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