package com.parkez.promotion.service;

import static com.parkez.promotion.domain.enums.PromotionType.*;
import static com.parkez.promotion.excption.PromotionErrorCode.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;

@ExtendWith(MockitoExtension.class)
class PromotionReaderTest {

	@Mock
	private PromotionRepository promotionRepository;

	@InjectMocks
	private PromotionReader promotionReader;

	@Nested
	class FindActivePromotions {

		@Test
		public void 진행중인_프로모션_목록_조회_정상적으로_조회_할_수_있다() {
			//given
			int page = 1;
			int size = 10;
			PageRequest pageRequest = PageRequest.of(page - 1, size);

			Long id = 1L;
			String promotionName = "신규가입 프로모션";
			PromotionType promotionType = DAILY;
			Integer limitPerUser = 1;
			LocalDateTime promotionStartAt = LocalDateTime.now().minusDays(1);
			LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(1);
			String couponName = "신규가입 쿠폰";
			Integer discountValue = 2000;

			ActivePromotionProjection projection = getActivePromotionDetailForUserProjection(id, promotionName, promotionType,
				limitPerUser, promotionStartAt, promotionEndAt, couponName, discountValue);

			given(promotionRepository.findActivePromotions(any(LocalDateTime.class), any(PromotionStatus.class), any(
				Pageable.class))).willReturn(new PageImpl<>(List.of(projection), pageRequest, 0));

			//when
			Page<ActivePromotionProjection> activePromotions = promotionReader.findAllCurrentlyActive(page, size);

			//then
			Assertions.assertThat(activePromotions.getContent()).hasSize(1);
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
		public void 진행중인_프로모션_목록_조회_없으면_빈_배열_반환() {
			//given
			int page = 1;
			int size = 10;
			PageRequest pageRequest = PageRequest.of(page - 1, size);

			given(promotionRepository.findActivePromotions(any(LocalDateTime.class), any(PromotionStatus.class), any(
				Pageable.class))).willReturn(new PageImpl<>(List.of(), pageRequest, 0));

			//when
			Page<ActivePromotionProjection> activePromotions = promotionReader.findAllCurrentlyActive(page, size);

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

			given(promotionRepository.findActivePromotionDetail(anyLong(), anyLong(),
				any(LocalDateTime.class), anyString())).willReturn(
				Optional.empty());

			//when & then
			Assertions.assertThatThrownBy(()-> promotionReader.getActivePromotionDetailForUser(userId, promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 진행중인_프로모션_단건_조회_성공적으로_프로모션_상세를_반환한다() {
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
			given(promotionRepository.findActivePromotionDetail(anyLong(), anyLong(),
				any(LocalDateTime.class), anyString())).willReturn(
				Optional.of(promotionDetail));

			//when
			PromotionDetail activePromotion = promotionReader.getActivePromotionDetailForUser(userId, promotionId);

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
	class GetActiveByIdWithCoupon {

		@Test
		public void 진행중인_프로모션_단건_조회_존재하지않는_프로모션이면_PROMOTION_NOT_FOUND_예외_발생() {
			//given
			Long promotionId = -1L;

			given(promotionRepository.findActivePromotionWithPessimisticLock(anyLong(),
				any(LocalDateTime.class), any(PromotionStatus.class))).willReturn(
				Optional.empty());

			//when & then
			Assertions.assertThatThrownBy(()-> promotionReader.getActivePromotionWithCouponForUpdate(promotionId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(PROMOTION_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 진행중인_프로모션_단건_조회_프로모션ID와_현재시각으로_포로모션을_정상적으로_조회한다() {
			//given
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
			given(promotionRepository.findActivePromotionWithPessimisticLock(anyLong(), any(LocalDateTime.class), any(PromotionStatus.class))).willReturn(
				Optional.of(promotion));

			//when
			Promotion result = promotionReader.getActivePromotionWithCouponForUpdate(promotionId);

			//then
			Assertions.assertThat(result)
				.extracting(
					"id", "name", "promotionType", "promotionStartAt",
					"promotionEndAt", "validDaysAfterIssue", "limitTotal", "limitPerUser",
					"coupon.id", "coupon.name", "coupon.discountValue", "coupon.discountType"
				).containsExactly(
					promotionId, promotionName, promotionType, promotionStartAt,
					promotionEndAt, validDaysAfterIssue, limitTotal, limitPerUser,
					couponId, couponName, discountValue, discountType
				);

		}
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

	private ActivePromotionProjection getActivePromotionDetailForUserProjection(Long id, String promotionName,
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