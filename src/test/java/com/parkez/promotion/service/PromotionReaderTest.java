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

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetailProjection;

@ExtendWith(MockitoExtension.class)
class PromotionReaderTest {

	@Mock
	private PromotionRepository promotionRepository;

	@InjectMocks
	private PromotionReader promotionReader;

	@Nested
	class FindActivePromotions {

		@Test
		public void 진행중인_프로모션_조회_한건_정상적으로_조회_할_수_있다() {
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

			ActivePromotionProjection projection = getActivePromotionProjection(id, promotionName, promotionType,
				limitPerUser, promotionStartAt, promotionEndAt, couponName, discountValue);

			given(promotionRepository.findActivePromotions(any(LocalDateTime.class), any(PromotionStatus.class), any(
				Pageable.class))).willReturn(new PageImpl<>(List.of(projection), pageRequest, 0));

			//when
			Page<ActivePromotionProjection> activePromotions = promotionReader.findActivePromotions(page, size);

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
		public void 진행중인_프로모션_조회_없으면_빈_배열_반환() {
			//given
			int page = 1;
			int size = 10;
			PageRequest pageRequest = PageRequest.of(page - 1, size);

			given(promotionRepository.findActivePromotions(any(LocalDateTime.class), any(PromotionStatus.class), any(
				Pageable.class))).willReturn(new PageImpl<>(List.of(), pageRequest, 0));

			//when
			Page<ActivePromotionProjection> activePromotions = promotionReader.findActivePromotions(page, size);

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

			given(promotionRepository.findActivePromotionDetailById(anyLong(), anyLong(),
				any(LocalDateTime.class), anyString())).willReturn(
				Optional.empty());

			//when & then
			Assertions.assertThatThrownBy(()-> promotionReader.getActivePromotion(userId, promotionId))
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
			PromotionDetailProjection promotionDetailProjection = getPromotionDetailProjection(id, promotionName,
				promotionType, promotionStartAt, promotionEndAt, validDaysAfterIssue, limitTotal, limitPerUser, couponName,
				discountValue, isAvailableToIssue, remainingQuantity, availableIssueCount, expiresAtIfIssuedNow);
			given(promotionRepository.findActivePromotionDetailById(anyLong(), anyLong(),
				any(LocalDateTime.class), anyString())).willReturn(
				Optional.of(promotionDetailProjection));

			//when
			PromotionDetailProjection activePromotion = promotionReader.getActivePromotion(userId, promotionId);

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

	private PromotionDetailProjection getPromotionDetailProjection(long id, String promotionName,
		PromotionType promotionType, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		int validDaysAfterIssue, int limitTotal, int limitPerUser, String couponName,
		int discountValue, Boolean isAvailableToIssue, int remainingQuantity,
		int availableIssueCount, LocalDateTime expiresAtIfIssuedNow) {
		return new PromotionDetailProjection() {
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

}