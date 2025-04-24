package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;
import com.parkez.promotion.dto.response.PromotionIssueResponse;
import com.parkez.promotion.dto.request.PromotionCreateRequest;
import com.parkez.promotion.dto.response.ActivePromotionResponse;
import com.parkez.promotion.dto.response.PromotionCreateResponse;
import com.parkez.promotion.dto.response.PromotionDetailResponse;
import com.parkez.promotion.excption.PromotionErrorCode;
import com.parkez.user.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionService {

	private final PromotionWriter promotionWriter;
	private final PromotionReader promotionReader;
	private final CouponReader couponReader;
	private final PromotionIssueWriter promotionIssueWriter;
	private final PromotionIssueReader promotionIssueReader;

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

		Page<ActivePromotionProjection> activePromotions = promotionReader.findAllCurrentlyActive(page, size);
		return activePromotions.map(ActivePromotionResponse::from);
	}

	public PromotionDetailResponse getActivePromotion(Long userId, Long promotionId) {

		PromotionDetail activePromotion = promotionReader.getActivePromotionDetailForUser(userId, promotionId);

		return PromotionDetailResponse.from(activePromotion);
	}

	@Transactional
	public PromotionIssueResponse issueCoupon(AuthUser authUser, Long promotionId) {

		Promotion promotion =  promotionReader.getActivePromotionWithCouponForUpdate(promotionId);

		// TODO 카운트 쿼리 묶을지
		int issuedCount = promotionIssueReader.countByPromotionId(promotionId);

		if (!promotion.hasRemainingQuantity(issuedCount)) {
			throw new ParkingEasyException(PromotionErrorCode.QUANTITY_EXCEEDED);
		}

		int userIssuedCount = promotionIssueReader.countByPromotionIdAndUserId(promotionId, authUser.getId());

		if (!promotion.canUserIssueMore(userIssuedCount)) {
			throw new ParkingEasyException(PromotionErrorCode.ALREADY_ISSUED);
		}

		User user = User.from(authUser);

		PromotionIssue promotionIssue = promotionIssueWriter.create(promotion, user);

		return PromotionIssueResponse.of(promotion, promotionIssue);

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
