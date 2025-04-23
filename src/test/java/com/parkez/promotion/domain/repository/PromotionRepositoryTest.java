package com.parkez.promotion.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetailProjection;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, PersistenceConfig.class})
class PromotionRepositoryTest {

	@Autowired
	private PromotionRepository promotionRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private PromotionIssueRepository promotionIssueRepository;

	@Autowired
	private UserRepository userRepository;

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
			assertThat(activePromotions.getContent()).isEmpty();

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
			assertThat(activePromotions.getContent()).isEmpty();

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
			assertThat(activePromotions.getContent()).isEmpty();

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
			assertThat(activePromotions.getContent()).isEmpty();

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
			LocalDateTime promotionStartAt = now.minusDays(1);
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
			assertThat(activePromotions.getContent()).hasSize(1);
			assertThat(activePromotions.getContent())
				.extracting(
					"promotionName", "promotionType", "limitPerUser", "promotionStartAt", "promotionEndAt",
					"discountValue", "couponName"
				).containsExactly(
					Tuple.tuple(promotionName, promotionType, limitPerUser, promotionStartAt, promotionEndAt,
						discountValue, couponName)
				);

		}
	}

	@Nested
	class FindActivePromotionDetailById {

		@Test
		public void 진행중인_프로모션_상세_조회_종료된_포로모션은_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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
			promotion.updateStatus(PromotionStatus.ENDED);
			Promotion savedPromotion = promotionRepository.save(promotion);

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			assertThat(activePromotionDetailById).isEmpty();

		}

		@Test
		public void 진행중인_프로모션_상세_조회_시작전이면_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now.plusDays(1);
			LocalDateTime promotionEndAt = now.plusDays(2);

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

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			assertThat(activePromotionDetailById).isEmpty();

		}

		@Test
		public void 진행중인_프로모션_상세_조회_기간이_지나면_조회되지_않는다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime promotionStartAt = now.minusDays(3);
			LocalDateTime promotionEndAt = now.minusDays(1);

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

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			assertThat(activePromotionDetailById).isEmpty();

		}

		@Test
		public void 진행중인_프로모션_상세_조회_유저당_쿠폰발급수_1회이고_미발급이면_발급가능상태_true를_반환한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"isAvailableToIssue"
				).isEqualTo(true);

		}

		@Test
		public void 진행중인_프로모션_상세_조회_유저당_쿠폰발급수_1회이고_특정_유저가_1회_발급_했을경우_발급가능상태_false를_반환한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"isAvailableToIssue"
				).isEqualTo(false);

		}

		@Test
		public void 진행중인_프로모션_상세_조회_발급_가능_회수_100회이고_1회_발급_했을경우_남은_발급_개수_99를_반환한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"remainingQuantity"
				).isEqualTo(99);

		}

		@Test
		public void 진행중인_프로모션_상세_조회_유저당_쿠폰발급수_1회이고_특정_유저가_1회_발급_했을경우_남은_발급_가능_개수_0을_반환한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime usedAt = null;
			PromotionIssue promotionIssue = createPromotionIssue(savedPromotion, savedUser, issuedAt, promotion, usedAt);

			promotionIssueRepository.save(promotionIssue);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"availableIssueCount"
				).isEqualTo(0);

		}

		@Test
		public void 진행중인_프로모션_상세_조회_유저당_쿠폰발급수_1회이고_특정_유저가_미발급이면_남은_발급_가능_개수_1을_반환한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), LocalDateTime.now(), PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"availableIssueCount"
				).isEqualTo(1);

		}

		@Test
		public void 진행중인_프로모션_상세조회에서_발급시점을_기준으로_유효일수가_더해진_만료일이_반환된다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;
			LocalDateTime now = LocalDateTime.now();
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

			LocalDateTime issuedAt = LocalDateTime.of(2025, 4, 23, 17, 26, 0);

			//when
			Optional<PromotionDetailProjection> activePromotionDetailById = promotionRepository.findActivePromotionDetailById(
				savedUser.getId(), savedPromotion.getId(), issuedAt, PromotionStatus.ACTIVE.name());

			//then
			Assertions.assertThat(activePromotionDetailById).isNotEmpty();
			Assertions.assertThat(activePromotionDetailById.get())
				.extracting(
					"expiresAtIfIssuedNow"
				).isEqualTo(issuedAt.plusDays(validDaysAfterIssue));

		}
	}

	private PromotionIssue createPromotionIssue(Promotion savedPromotion, User savedUser, LocalDateTime issuedAt,
		Promotion promotion, LocalDateTime usedAt) {
		return PromotionIssue.builder()
			.promotion(savedPromotion)
			.user(savedUser)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plusDays(promotion.getValidDaysAfterIssue()))
			.usedAt(usedAt)
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