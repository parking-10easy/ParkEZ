package com.parkez.promotion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.repository.CouponRepository;
import com.parkez.promotion.dto.request.CouponCreateRequest;

@ExtendWith(MockitoExtension.class)
class CouponWriterTest {

	@Mock
	private CouponRepository couponRepository;

	@InjectMocks
	private CouponWriter couponWriter;

	@Nested
	class Create {

		@Test
		public void 쿠폰생성_유효한_요청으로_쿠폰을_생성하고_저장한다() {
			//given
			String name = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.FIXED;
			int discountValue = 2000;
			String description = "신규 유저 전용, 1회만 사용 가능";
			Long couponId = 1L;

			Coupon coupon = createCoupon(couponId, name, discountType, discountValue, description);


			given(couponRepository.save(any(Coupon.class))).willReturn(coupon);

			//when
			Coupon result = couponWriter.create(name, description, discountValue, discountType);

			//then
			Assertions.assertThat(result)
				.extracting(
					"id", "name", "discountType", "discountValue"
				).containsExactly(
					couponId,name,discountType, discountValue
				);

		}
	}

	private Coupon createCoupon(Long id, String name, DiscountType discountType, int discountValue, String description) {
		Coupon coupon = Coupon.builder()
			.name(name)
			.discountType(discountType)
			.discountValue(discountValue)
			.description(description)
			.build();
		ReflectionTestUtils.setField(coupon, "id", id);
		return coupon;
	}

	private CouponCreateRequest createCouponCreateRequest(String name, DiscountType discountType,
		int discountValue, String description) {
		return CouponCreateRequest.builder()
			.name(name)
			.discountType(discountType)
			.discountValue(discountValue)
			.description(description)
			.build();
	}

}