package com.parkez.promotion.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.promotion.domain.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
