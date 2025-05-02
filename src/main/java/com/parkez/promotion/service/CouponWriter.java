package com.parkez.promotion.service;

import org.springframework.stereotype.Service;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.repository.CouponRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponWriter {

	private final CouponRepository couponRepository;

	public Coupon create(String name, String description, Integer discountValue, DiscountType discountType) {
		Coupon coupon = Coupon.builder()
			.name(name)
			.description(description)
			.discountType(discountType)
			.discountValue(discountValue)
			.build();

		return couponRepository.save(coupon);
	}
}
