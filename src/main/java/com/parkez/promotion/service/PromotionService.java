package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.dto.request.PromotionCreateRequest;
import com.parkez.promotion.dto.response.ActivePromotionResponse;
import com.parkez.promotion.dto.response.PromotionCreateResponse;
import com.parkez.promotion.excption.PromotionErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionService {

	private final PromotionWriter promotionWriter;
	private final PromotionReader promotionReader;
	private final CouponReader couponReader;

	@Transactional
	public PromotionCreateResponse createPromotion(PromotionCreateRequest request) {

		validateDateRange(request.getPromotionStartAt(), request.getPromotionEndAt());

		validateStartDateNotPast(request.getPromotionStartAt(), LocalDateTime.now());

		Coupon coupon = couponReader.getById(request.getCouponId());

		Promotion promotion = promotionWriter.create(coupon, request.getName(), request.getPromotionType(),
			request.getLimitTotal(), request.getLimitPerUser(), request.getPromotionStartAt(),
			request.getPromotionEndAt(), request.getValidDaysAfterIssue());
		return PromotionCreateResponse.from(promotion);
	}

	public Page<ActivePromotionResponse> getActivePromotions(int page, int size) {

		Page<ActivePromotionProjection> activePromotions = promotionReader.findActivePromotions(page, size);
		return activePromotions.map(ActivePromotionResponse::from);
	}

	private void validateDateRange(LocalDateTime promotionStartAt, LocalDateTime promotionEndAt) {
		if (promotionStartAt.isAfter(promotionEndAt)) {
			throw new ParkingEasyException(PromotionErrorCode.INVALID_DATE_RANGE);
		}
	}

	private void validateStartDateNotPast(LocalDateTime promotionStartAt, LocalDateTime now) {
		if (promotionStartAt.isBefore(now)) {
			throw new ParkingEasyException(PromotionErrorCode.INVALID_START_DATE);
		}
	}

}
