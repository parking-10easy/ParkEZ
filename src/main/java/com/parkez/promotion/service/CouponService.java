package com.parkez.promotion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.dto.request.CouponCreateRequest;
import com.parkez.promotion.dto.response.CouponResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {

	private final CouponWriter couponWriter;

	@Transactional
	public CouponResponse createCoupon(CouponCreateRequest request) {

		Coupon coupon = couponWriter.create(request.getName(),request.getDescription(),request.getDiscountValue(),request.getDiscountType());

		return CouponResponse.from(coupon);
	}

}
