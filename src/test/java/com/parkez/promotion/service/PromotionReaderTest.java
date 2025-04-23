package com.parkez.promotion.service;

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
import org.springframework.data.domain.Pageable;

import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;

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
			PromotionType promotionType = PromotionType.DAILY;
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