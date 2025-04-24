package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;
import com.parkez.promotion.excption.PromotionErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionReader {

	private final PromotionRepository promotionRepository;

	public Page<ActivePromotionProjection> findAllCurrentlyActive(int page, int size) {
		LocalDateTime now = LocalDateTime.now();
		Pageable pageable = PageRequest.of(page - 1, size);
		return promotionRepository.findActivePromotions(now, PromotionStatus.ACTIVE, pageable);
	}

	public PromotionDetail getActivePromotionDetailForUser(Long userId, Long promotionId) {

		LocalDateTime issuedAt = LocalDateTime.now();
		String active = PromotionStatus.ACTIVE.name();

		return promotionRepository.findActivePromotionDetail(userId, promotionId, issuedAt, active).orElseThrow(
			()-> new ParkingEasyException(PromotionErrorCode.PROMOTION_NOT_FOUND)
		);

	}

	public Promotion getActiveByIdWithCoupon(Long promotionId) {
		LocalDateTime now = LocalDateTime.now();
		PromotionStatus active = PromotionStatus.ACTIVE;
		return promotionRepository.findActivePromotion(promotionId, now, active).orElseThrow(
			()-> new ParkingEasyException(PromotionErrorCode.PROMOTION_NOT_FOUND)
		);
	}

}
