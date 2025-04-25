package com.parkez.promotion.service;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.dto.request.MyCouponSearchCondition;
import com.parkez.promotion.dto.response.MyCouponResponse;

@ExtendWith(MockitoExtension.class)
class PromotionIssueServiceTest {

	@Mock
	private PromotionIssueReader promotionIssueReader;

	@Mock
	private PromotionIssueWriter promotionIssueWriter;

	@InjectMocks
	private PromotionIssueService promotionIssueService;

	@Nested
	class GetMyIssuedCoupons {

		@Test
		public void 특정_유저의_발급된_쿠폰_목록을_정상적으로_조회할_수_있다() {
			//given
			Long userId = 1L;
			int page = 1;
			int size = 10;

			MyCouponSearchCondition condition = createMyCouponSearchCondition();

			long promotionId = 1L;
			String promotionName = "DAILY 할인 이벤트";
			String couponName = "WELCOME 10%";
			int discountValue = 10;
			DiscountType discountType = DiscountType.PERCENT;
			LocalDateTime issuedAt = LocalDateTime.now();
			LocalDateTime expiresAt = issuedAt.plusDays(3);
			LocalDateTime usedAt = null;

			List<MyCouponResponse> responseList = List.of(createMyCouponResponse(promotionId, promotionName, couponName,
				discountValue, discountType, issuedAt, expiresAt, usedAt));

			Page<MyCouponResponse> myCouponResponses = new PageImpl<>(responseList, PageRequest.of(page, size),
				responseList.size());

			given(promotionIssueReader.findMyIssuedCoupons(anyLong(),any(PromotionIssueStatus.class), any(Pageable.class), any(PromotionIssueSortBy.class), any(
				Sort.Direction.class))).willReturn(myCouponResponses);

			//when
			Page<MyCouponResponse> myIssuedCoupons = promotionIssueService.getMyIssuedCoupons(userId, condition, page, size);

			//then
			Assertions.assertThat(myIssuedCoupons.getContent()).hasSize(1);
			Assertions.assertThat(myIssuedCoupons.getContent())
				.extracting(
					"promotionId", "promotionName", "couponName", "discountValue", "discountType", "issuedAt", "expiresAt", "usedAt"
				).containsExactly(
					Tuple.tuple(promotionId, promotionName, couponName, discountValue, discountType, issuedAt, expiresAt, usedAt)
				);

		}
	}

	@Nested
	class ExpirePromotionIssues {

		@Test
		public void 만료시간이_지난_ISSUED_쿠폰_1건을_만료처리하고_건수를_반환한다() {
			//given
			LocalDateTime currentDateTime = LocalDateTime.now();
			PromotionIssueStatus currentStatus = PromotionIssueStatus.ISSUED;
			PromotionIssueStatus targetStatus = PromotionIssueStatus.EXPIRED;

			given(promotionIssueWriter.expirePromotionIssues(any(LocalDateTime.class), any(PromotionIssueStatus.class), any(
				PromotionIssueStatus.class))).willReturn(1);

			//when
			int expiredPromotionIssuesCount = promotionIssueService.expirePromotionIssues(currentDateTime, currentStatus, targetStatus);

			//then
			assertThat(expiredPromotionIssuesCount).isEqualTo(1);

		}
	}

	private static MyCouponResponse createMyCouponResponse(long promotionId, String promotionName, String couponName,
		int discountValue, DiscountType discountType, LocalDateTime issuedAt, LocalDateTime expiresAt,
		LocalDateTime usedAt) {
		return MyCouponResponse.builder()
			.promotionId(promotionId)
			.promotionName(promotionName)
			.couponName(couponName)
			.discountValue(discountValue)
			.discountType(discountType)
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.usedAt(usedAt)
			.build();
	}

	private static MyCouponSearchCondition createMyCouponSearchCondition() {
		return MyCouponSearchCondition.builder()
			.direction(Sort.Direction.DESC)
			.sortBy(PromotionIssueSortBy.ISSUED_AT)
			.status(PromotionIssueStatus.ISSUED)
			.build();
	}

}