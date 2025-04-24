package com.parkez.promotion.concurrency;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.parkez.common.principal.AuthUser;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.CouponRepository;
import com.parkez.promotion.domain.repository.PromotionIssueRepository;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.service.PromotionIssueReader;
import com.parkez.promotion.service.PromotionService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PromotionIssueConcurrencyTest {

	/*
		목표 시나리오
		프로모션 쿠폰 개수 100개
		유저당 1개 발급 가능
		100명 이상이 동시에 발급 요청 -> 결과적으로 저장된 이력 수가 100개 인지 확인
	 */

	/*
		테스트 방법
		ExecutorService로 100개 쓰레드 동시에 실행
		CountDownLatch로 발사
		promotionIssueService.issueCoupon() 호출
		중복 발급 / 초과 발급 방지 검증
	 */

	@Autowired
	private PromotionService promotionService;

	@Autowired
	private PromotionIssueReader promotionIssueReader;

	@Autowired
	private PromotionIssueRepository promotionIssueRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private PromotionRepository promotionRepository;
	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void tearDown() {
		promotionIssueRepository.deleteAll();
		promotionRepository.deleteAll();
		couponRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	public void 동일한_유저가_여러번_요청해도_쿠폰은_1회만_발급된다() throws InterruptedException {
		//given
		Coupon coupon = createCoupon();
		Promotion promotion = createPromotion(coupon);
		User user = createUser();

		AuthUser authUser = AuthUser.builder()
			.nickname(user.getNickname())
			.roleName(user.getRoleName())
			.email(user.getEmail())
			.id(user.getId())
			.build();

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch latch = new CountDownLatch(threadCount);

		//when
		for (int i = 0; i < threadCount; i++) {
			executorService.execute(() -> {
				try {
					promotionService.issueCoupon(authUser, promotion.getId());
				} catch (Exception e) {
					System.out.println("예외 발생: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		int count = promotionIssueReader.countByPromotionIdAndUserId(promotion.getId(), user.getId());
		assertThat(count).isEqualTo(1);

	}

	@Test
	public void 동시에_여러_유저가_쿠폰을_요청해도_100장만_발급된다() throws InterruptedException {
		//given
		final int promotionLimit = 100;
		final int userCount = 200;

		Coupon coupon = createCoupon();
		Promotion promotion = createPromotion(coupon);

		List<User> users = createUsers(userCount);
		userRepository.saveAll(users);
		List<AuthUser> authUsers = convertToAuthUsers(users);

		int threadCount = 200;
		ExecutorService executorService = Executors.newFixedThreadPool(200);
		CountDownLatch latch = new CountDownLatch(threadCount);

		//when
		for (int i = 0; i < threadCount; i++) {
			final int finalIndex = i;
			executorService.execute(() -> {
				try {
					promotionService.issueCoupon(authUsers.get(finalIndex), promotion.getId());
				} catch (Exception e) {
					System.out.println("예외 발생: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		//then
		int count = promotionIssueReader.countByPromotionId(promotion.getId());
		assertThat(count).isEqualTo(promotionLimit);

	}

	private List<AuthUser> convertToAuthUsers(List<User> users) {
		List<AuthUser> authUsers = new ArrayList<>();
		for (User user : users) {
			authUsers.add(AuthUser.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.roleName(user.getRoleName())
				.build());
		}
		return authUsers;
	}

	private List<User> createUsers(int userCount) {
		List<User> users = new ArrayList<>();
		for (int i = 0; i < userCount; i++) {
			User user = User.createUser(
				"user" + i + "@test.com",
				"password",
				"nickname" + i,
				"010-0000-000" + i,
				"default.jpg"
			);
			users.add(user);
		}
		return users;
	}

	private User createUser() {
		return userRepository.save(
			User.createUser("user@example.com", "password", "nickname", "010-1234-5678", "default.jpg"));
	}

	private Coupon createCoupon() {
		return couponRepository.save(Coupon.builder()
			.name("신규가입 2000원 할인 쿠폰")
			.discountType(DiscountType.PERCENT)
			.discountValue(10)
			.description("신규 유저 전용, 1회만 사용 가능")
			.build());
	}

	private Promotion createPromotion(Coupon coupon) {
		LocalDateTime now = LocalDateTime.now();
		return promotionRepository.save(Promotion.builder()
			.name("DAILY 2000")
			.promotionType(PromotionType.DAILY)
			.coupon(coupon)
			.limitTotal(100)
			.limitPerUser(1)
			.promotionStartAt(now.minusDays(1))
			.promotionEndAt(now.plusDays(1))
			.validDaysAfterIssue(3)
			.build());
	}

}
