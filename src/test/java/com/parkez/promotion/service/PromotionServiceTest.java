package com.parkez.promotion.service;

import static com.parkez.promotion.domain.enums.PromotionType.*;
import static com.parkez.promotion.excption.PromotionErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;
import com.parkez.promotion.dto.request.PromotionCreateRequest;
import com.parkez.promotion.dto.response.ActivePromotionResponse;
import com.parkez.promotion.dto.response.PromotionCreateResponse;
import com.parkez.promotion.dto.response.PromotionDetailResponse;
import com.parkez.promotion.dto.response.PromotionIssueResponse;
import com.parkez.promotion.excption.CouponErrorCode;
import com.parkez.promotion.excption.PromotionErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

	@Mock
	private PromotionWriter promotionWriter;

	@Mock
	private CouponReader couponReader;

	@Mock
	private PromotionReader promotionReader;

	@Mock
	private PromotionIssueReader promotionIssueReader;

	@Mock
	private PromotionIssueWriter promotionIssueWriter;

	@InjectMocks
	private PromotionService promotionService;

	@Nested
	class CreatePromotion {

		@Test
		public void 프로모션_생성_프로모션_시작일이_과거이면_INVALID_START_DATE_예외_발생한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			LocalDateTime wrongPromotionStartAt = LocalDateTime.now().minusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(1);
			long couponId = 1L;
			int validDaysAfterIssue = 3;
			PromotionCreateRequest request = createPromotionCreateRequest(promotionName, promotionType, couponId,
				limitTotal, limitPerUser, wrongPromotionStartAt, promotionEndAt, validDaysAfterIssue);

			//when & then
			Assertions.assertThatThrownBy(() -> promotionService.createPromotion(request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PromotionErrorCode.INVALID_START_DATE.getDefaultMessage());

		}

		@Test
		public void 프로모션_생성_프로모션_시작일이_종료일_이후이면_INVALID_DATE_RANGE_예외_발생한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			LocalDateTime wrongPromotionStartAt = LocalDateTime.now().plusDays(2);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(1);
			long couponId = 1L;
			int validDaysAfterIssue = 3;
			PromotionCreateRequest request = createPromotionCreateRequest(promotionName, promotionType, couponId,
				limitTotal, limitPerUser, wrongPromotionStartAt, promotionEndAt, validDaysAfterIssue);

			//when & then
			Assertions.assertThatThrownBy(() -> promotionService.createPromotion(request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PromotionErrorCode.INVALID_DATE_RANGE.getDefaultMessage());

		}

		@Test
		public void 프로모션_생성_존재하지_않는_쿠폰이면_COUPON_NOT_FOUND_예외_발생한다() {
			//given
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);
			long wrongCouponId = -1;
			int validDaysAfterIssue = 3;
			PromotionCreateRequest request = createPromotionCreateRequest(promotionName, promotionType, wrongCouponId,
				limitTotal, limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue);

			given(couponReader.getById(anyLong())).willThrow(
				new ParkingEasyException(CouponErrorCode.COUPON_NOT_FOUND));

			//when & then
			Assertions.assertThatThrownBy(() -> promotionService.createPromotion(request))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(CouponErrorCode.COUPON_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 프로모션_생성_유효한_요청값으로_프로모션을_생성하고_응답을_반환한다() {
			//given
			long promotionId = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 1000;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;

			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			long couponId = 1L;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";

			PromotionCreateRequest request = createPromotionCreateRequest(promotionName, promotionType, couponId,
				limitTotal,
				limitPerUser,
				promotionStartAt, promotionEndAt, validDaysAfterIssue);
			Coupon coupon = createCoupon(couponId, couponName, discountType, discountValue, description);

			Promotion promotion = createPromotion(promotionId, promotionName, promotionType, coupon, limitTotal,
				limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue, PromotionStatus.ACTIVE);

			given(couponReader.getById(anyLong())).willReturn(coupon);
			given(promotionWriter.create(any(Coupon.class), anyString(), any(PromotionType.class), anyInt(), anyInt(),
				any(
					LocalDateTime.class), any(LocalDateTime.class), anyInt())).willReturn(promotion);

			//when
			PromotionCreateResponse promotionCreateResponse = promotionService.createPromotion(request);

			//then
			Assertions.assertThat(promotionCreateResponse)
				.extracting(
					"id", "name", "promotionType", "couponResponse.id", "couponResponse.name",
					"couponResponse.discountType", "couponResponse.discountValue", "limitTotal", "limitPerUser",
					"promotionStartAt", "promotionEndAt", "validDaysAfterIssue"
				)
				.containsExactly(
					promotionId, promotionName, promotionType, couponId, couponName, discountType, discountValue,
					limitTotal, limitPerUser, promotionStartAt, promotionEndAt, validDaysAfterIssue
				);
		}

	}

	@Nested
	class GetActivePromotions {

		@Test
		public void 진행중인_프로모션_조회_한건_정상적으로_조회_할_수_있다() {
			//given
			int page = 1;
			int size = 10;
			PageRequest pageRequest = PageRequest.of(page - 1, size);

			Long id = 1L;
			String promotionName = "신규가입 프로모션";
			PromotionType promotionType = PromotionType.DAILY;
			Integer limitPerUser = 1;
			LocalDateTime promotionStartAt = LocalDateTime.now().minusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(1);
			String couponName = "신규가입 쿠폰";
			Integer discountValue = 2000;

			ActivePromotionProjection projection = getActivePromotionProjection(id, promotionName, promotionType,
				limitPerUser, promotionStartAt, promotionEndAt, couponName, discountValue);

			given(promotionReader.findAllCurrentlyActive(anyInt(),anyInt())).willReturn(new PageImpl<>(List.of(projection), pageRequest, 0));

			//when
			Page<ActivePromotionResponse> activePromotions = promotionService.getActivePromotions(page, size);

			//then
			Assertions.assertThat(activePromotions).size().isEqualTo(1);
			Assertions.assertThat(activePromotions.getContent())
				.extracting(
					"id", "promotionName", "promotionType", "limitPerUser", "promotionStartAt", "promotionEndAt",
					"discountValue", "couponName"
				).containsExactly(
					Tuple.tuple(id, promotionName, promotionType, limitPerUser, promotionStartAt, promotionEndAt,
						discountValue, couponName)
				);

		}

		@Test
		public void 진행중인_프로모션_조회_없으면_빈_배열_반환() {
			//given
			int page = 1;
			int size = 10;
			PageRequest pageRequest = PageRequest.of(page - 1, size);

			given(promotionReader.findAllCurrentlyActive(anyInt(),anyInt())).willReturn(new PageImpl<>(List.of(), pageRequest, 0));

			//when
			Page<ActivePromotionResponse> activePromotions = promotionService.getActivePromotions(page, size);

			//then
			Assertions.assertThat(activePromotions.getContent()).isEmpty();

		}
	}

	@Nested
	class GetActivePromotion {

		@Test
		public void 진행중인_프로모션_단건_조회_존재하지않는_프로모션이면_PROMOTION_NOT_FOUND_예외_발생() {
			//given
			Long userId = 1L;
			Long promotionId = -1L;

			given(promotionReader.getActivePromotionDetailForUser(anyLong(), anyLong())).willThrow(
				new ParkingEasyException(PROMOTION_NOT_FOUND));

			//when & then
			Assertions.assertThatThrownBy(()-> promotionService.getActivePromotion(userId, promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 진행중인_프로모션_단건_조회_성공적으로_프로모션_상세_프로젝션을_반환한다() {
			//given
			Long userId = 1L;
			Long promotionId = 1L;

			long id = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = DAILY;
			LocalDateTime promotionStartAt = LocalDateTime.now().minusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);
			int validDaysAfterIssue = 3;
			int limitTotal = 100;
			int limitPerUser = 1;
			String couponName = "신규가입 2000원 할인 쿠폰";
			int discountValue = 2000;
			Boolean isAvailableToIssue = true;
			int remainingQuantity = 22;
			int availableIssueCount = 1;
			LocalDateTime expiresAtIfIssuedNow = LocalDateTime.now().plusDays(3);
			PromotionDetail promotionDetail = getPromotionDetailProjection(id, promotionName,
				promotionType, promotionStartAt, promotionEndAt, validDaysAfterIssue, limitTotal, limitPerUser, couponName,
				discountValue, isAvailableToIssue, remainingQuantity, availableIssueCount, expiresAtIfIssuedNow);
			given(promotionReader.getActivePromotionDetailForUser(anyLong(), anyLong())).willReturn(promotionDetail);

			//when
			PromotionDetailResponse activePromotion = promotionService.getActivePromotion(userId, promotionId);

			//then
			Assertions.assertThat(activePromotion)
				.extracting(
					"id", "promotionName", "promotionType", "promotionStartAt",
					"promotionEndAt", "validDaysAfterIssue", "limitTotal", "limitPerUser",
					"couponName", "discountValue", "isAvailableToIssue", "remainingQuantity",
					"availableIssueCount", "expiresAtIfIssuedNow"
				).containsExactly(
					id, promotionName, promotionType, promotionStartAt,
					promotionEndAt, validDaysAfterIssue, limitTotal, limitPerUser,
					couponName, discountValue, isAvailableToIssue, remainingQuantity,
					availableIssueCount, expiresAtIfIssuedNow
				);

		}
	}

	@Nested
	class IssueCoupon {

		@Test
		public void 쿠폰_발급_존재하지_않는_프로모션이면_PROMOTION_NOT_FOUND_예외_발생한다() {
			//given
			AuthUser authUser = creatAuthUser();
			Long promotionId = -1L;


			given(promotionReader.getActivePromotionWithCouponForUpdate(anyLong())).willThrow(new ParkingEasyException(PROMOTION_NOT_FOUND));

			//when & then
			Assertions.assertThatThrownBy(()-> promotionService.issueCoupon(authUser, promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 쿠폰_발급_남은_쿠폰개수가_0이면_QUANTITY_EXCEEDED_예외_발생한다() {
			//given
			AuthUser authUser = creatAuthUser();
			Long promotionId = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;

			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			long couponId = 1L;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";



			Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
			Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

			int issuedCount = 100;

			given(promotionReader.getActivePromotionWithCouponForUpdate(anyLong())).willReturn(promotion);
			given(promotionIssueReader.countByPromotionId(anyLong())).willReturn(issuedCount);

			//when & then
			Assertions.assertThatThrownBy(()-> promotionService.issueCoupon(authUser, promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(QUANTITY_EXCEEDED.getDefaultMessage());

		}

		@Test
		public void 쿠폰_발급_유저당_1회로_제한된_상황에서_중복_요청시_ALREADY_ISSUED_예외_발생() {
			//given
			AuthUser authUser = creatAuthUser();
			Long promotionId = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;

			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			long couponId = 1L;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";



			Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
			Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

			int issuedCount = 99;
			int userIssuedCount = 1;

			given(promotionReader.getActivePromotionWithCouponForUpdate(anyLong())).willReturn(promotion);
			given(promotionIssueReader.countByPromotionId(anyLong())).willReturn(issuedCount);
			given(promotionIssueReader.countByPromotionIdAndUserId(anyLong(),anyLong())).willReturn(userIssuedCount);

			//when & then
			Assertions.assertThatThrownBy(()-> promotionService.issueCoupon(authUser, promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(ALREADY_ISSUED.getDefaultMessage());

		}

		@Test
		public void 쿠폰_발급_정상적으로_발급할_수_있다() {
			//given
			AuthUser authUser = creatAuthUser();
			Long promotionId = 1L;
			String promotionName = "DAILY 2000";
			PromotionType promotionType = PromotionType.DAILY;
			int limitTotal = 100;
			int limitPerUser = 1;
			int validDaysAfterIssue = 3;

			LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

			long couponId = 1L;
			String couponName = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.PERCENT;
			int discountValue = 10;
			String description = "신규 유저 전용, 1회만 사용 가능";



			Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
			Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
			PromotionIssue promotionIssue = createPromotionIssue(promotion, authUser, issuedAt, expiresAt);

			int issuedCount = 99;
			int userIssuedCount = 0;

			given(promotionReader.getActivePromotionWithCouponForUpdate(anyLong())).willReturn(promotion);
			given(promotionIssueReader.countByPromotionId(anyLong())).willReturn(issuedCount);
			given(promotionIssueReader.countByPromotionIdAndUserId(anyLong(),anyLong())).willReturn(userIssuedCount);
			given(promotionIssueWriter.create(any(Promotion.class), any(User.class))).willReturn(promotionIssue);


			//when
			PromotionIssueResponse promotionIssueResponse = promotionService.issueCoupon(authUser, promotionId);

			//then
			Assertions.assertThat(promotionIssueResponse)
				.extracting(
					"promotionId", "couponId", "couponName","issuedAt","expiresAt"
				).containsExactly(
					promotionId, couponId, couponName, issuedAt, expiresAt
				);

		}
	}

	@Nested
	class ExpireEndedPromotions {

		@Test
		public void 종료된_프로모션_1건을_만료처리하고_건수를_반환한다() {
			//given

			LocalDateTime currentDateTime = LocalDateTime.now();
			PromotionStatus currentStatus = PromotionStatus.ACTIVE;
			PromotionStatus targetStatus = PromotionStatus.ENDED;

			given(promotionWriter.expireEndedPromotions(any(LocalDateTime.class), any(PromotionStatus.class), any(PromotionStatus.class))).willReturn(1);

			//when
			int endedPromotionCount = promotionService.expireEndedPromotions(currentDateTime, currentStatus, targetStatus);

			//then
			assertThat(endedPromotionCount).isEqualTo(1);
		}
	}

	@Nested
	class ExpireSoldOutPromotionStatus {

		@Test
		public void 쿠폰_매진된_프로모션_1건을_만료처리하고_건수를_반환한다() {
			//given

			PromotionStatus currentStatus = PromotionStatus.ACTIVE;
			PromotionStatus targetStatus = PromotionStatus.ENDED;

			given(promotionWriter.expireSoldOutPromotions(any(PromotionStatus.class), any(PromotionStatus.class))).willReturn(1);

			//when
			int soldOutCount = promotionService.expireSoldOutPromotions(currentStatus, targetStatus);

			//then
			assertThat(soldOutCount).isEqualTo(1);
		}
	}

	private static PromotionIssue createPromotionIssue(Promotion promotion, AuthUser authUser, LocalDateTime issuedAt,
		LocalDateTime expiresAt) {
		return PromotionIssue.builder()
			.promotion(promotion)
			.user(User.from(authUser))
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.build();
	}

	private static AuthUser creatAuthUser() {
		return AuthUser.builder().email("user@example.com")
			.roleName(UserRole.ROLE_USER.name())
			.nickname("nickname")
			.id(1L).build();
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

	private PromotionCreateRequest createPromotionCreateRequest(String name, PromotionType promotionType,
		long couponId, int limitTotal, int limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		int validDaysAfterIssue) {
		return PromotionCreateRequest.builder()
			.name(name)
			.promotionType(promotionType)
			.couponId(couponId)
			.limitTotal(limitTotal)
			.limitPerUser(limitPerUser)
			.promotionStartAt(promotionStartAt)
			.promotionEndAt(promotionEndAt)
			.validDaysAfterIssue(validDaysAfterIssue)
			.build();
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

	private ActivePromotionProjection getActivePromotionProjection(Long id, String promotionName,
		PromotionType promotionType, Integer limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		String couponName, Integer discountValue) {
		return new ActivePromotionProjection() {
			public Long getId() {
				return id;
			}

			public String getPromotionName() {
				return promotionName;
			}

			public PromotionType getPromotionType() {
				return promotionType;
			}

			public Integer getLimitPerUser() {
				return limitPerUser;
			}

			public LocalDateTime getPromotionStartAt() {
				return promotionStartAt;
			}

			public LocalDateTime getPromotionEndAt() {
				return promotionEndAt;
			}

			public String getCouponName() {
				return couponName;
			}

			public Integer getDiscountValue() {
				return discountValue;
			}
		};
	}

	private PromotionDetail getPromotionDetailProjection(long id, String promotionName,
		PromotionType promotionType, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		int validDaysAfterIssue, int limitTotal, int limitPerUser, String couponName,
		int discountValue, Boolean isAvailableToIssue, int remainingQuantity,
		int availableIssueCount, LocalDateTime expiresAtIfIssuedNow) {
		return new PromotionDetail() {
			@Override
			public Long getId() {
				return id;
			}

			@Override
			public String getPromotionName() {
				return promotionName;
			}

			@Override
			public PromotionType getPromotionType() {
				return promotionType;
			}

			@Override
			public LocalDateTime getPromotionStartAt() {
				return promotionStartAt;
			}

			@Override
			public LocalDateTime getPromotionEndAt() {
				return promotionEndAt;
			}

			@Override
			public Integer getValidDaysAfterIssue() {
				return validDaysAfterIssue;
			}

			@Override
			public Integer getLimitTotal() {
				return limitTotal;
			}

			@Override
			public Integer getLimitPerUser() {
				return limitPerUser;
			}

			@Override
			public String getCouponName() {
				return couponName;
			}

			@Override
			public Integer getDiscountValue() {
				return discountValue;
			}

			@Override
			public Boolean getIsAvailableToIssue() {
				return isAvailableToIssue;
			}

			@Override
			public Integer getRemainingQuantity() {
				return remainingQuantity;
			}

			@Override
			public Integer getAvailableIssueCount() {
				return availableIssueCount;
			}

			@Override
			public LocalDateTime getExpiresAtIfIssuedNow() {
				return expiresAtIfIssuedNow;
			}
		};
	}

}