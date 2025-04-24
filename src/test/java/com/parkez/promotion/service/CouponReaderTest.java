package com.parkez.promotion.service;

import static com.parkez.promotion.excption.CouponErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.repository.CouponRepository;

@ExtendWith(MockitoExtension.class)
class CouponReaderTest {

	@Mock
	private CouponRepository couponRepository;

	@InjectMocks
	private CouponReader couponReader;

	@Nested
	class GetById {

		@Test
		public void 쿠폰_조회_존재하지_않는_아이디로_쿠폰_조회하면_COUPON_NOT_FOUND_예외_발생한다() {
			//given
			Long couponId = -1L;

			given(couponRepository.findById(anyLong())).willReturn(Optional.empty());

			//when & then
			Assertions.assertThatThrownBy(()-> couponReader.getById(couponId))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessage(COUPON_NOT_FOUND.getDefaultMessage());

		}

		@Test
		public void 쿠폰_조회_아이디로_쿠폰조회하면_쿠폰을_반환한다() {
			//given
			Long couponId = 1L;
			String name = "신규가입 2000원 할인 쿠폰";
			DiscountType discountType = DiscountType.FIXED;
			int discountValue = 2000;
			String description = "신규 유저 전용, 1회만 사용 가능";

			Coupon coupon = createCoupon(couponId,  name, discountType, discountValue, description);

			given(couponRepository.findById(anyLong())).willReturn(Optional.of(coupon));

			//when
			Coupon result = couponReader.getById(couponId);

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

}