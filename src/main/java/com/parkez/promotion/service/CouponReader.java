package com.parkez.promotion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.repository.CouponRepository;
import com.parkez.promotion.excption.CouponErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponReader {

	private final CouponRepository couponRepository;

	public Coupon getById(Long couponId) {
		return couponRepository.findById(couponId).orElseThrow(()-> new ParkingEasyException(CouponErrorCode.COUPON_NOT_FOUND));
	}
}
