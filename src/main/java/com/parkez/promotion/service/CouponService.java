package com.parkez.promotion.service;

import static com.parkez.promotion.excption.CouponErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.dto.request.CouponCreateRequest;
import com.parkez.promotion.dto.response.CouponResponse;
import com.parkez.promotion.excption.CouponErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

	private final CouponWriter couponWriter;

	// 할인율은 최대 20%까지만 허용 (정책상 제한)
	private static final int MAX_PERCENT_DISCOUNT = 20;
	// 고정 할인은 최소 1000원 이상부터 적용 가능 (정책상 제한)
	private static final int MIN_FIXED_DISCOUNT = 1000;


	@Transactional
	public CouponResponse createCoupon(CouponCreateRequest request) {

		validateDiscountPolicy(request.getDiscountType(), request.getDiscountValue());

		Coupon coupon = couponWriter.create(request.getName(),request.getDescription(),request.getDiscountValue(),request.getDiscountType());

		return CouponResponse.from(coupon);
	}

	private void validateDiscountPolicy(DiscountType discountType, Integer discountValue) {
		if (discountType == DiscountType.PERCENT) {
			if (discountValue > MAX_PERCENT_DISCOUNT) {
				throw new ParkingEasyException(INVALID_DISCOUNT_PERCENT_VALUE);
			}
		}

		if (discountType == DiscountType.FIXED) {
			if (discountValue < MIN_FIXED_DISCOUNT) {
				throw new ParkingEasyException(CouponErrorCode.INVALID_DISCOUNT_FIXED_VALUE);
			}
		}
	}

}
