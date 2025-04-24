package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.PromotionRepository;
import com.parkez.promotion.domain.repository.projection.PromotionDetailProjection;
import com.parkez.promotion.excption.PromotionErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromotionReader {

	private final PromotionRepository promotionRepository;

	public Page<ActivePromotionProjection> findActivePromotions(int page, int size) {
		LocalDateTime now = LocalDateTime.now();
		Pageable pageable = PageRequest.of(page - 1, size);
		return promotionRepository.findActivePromotions(now, PromotionStatus.ACTIVE, pageable);
	}

	// TODO 메서드명 의도를 드러내게 변경 해야함
	public PromotionDetailProjection getActivePromotion(Long userId, Long promotionId) {

		LocalDateTime issuedAt = LocalDateTime.now();
		String active = PromotionStatus.ACTIVE.name();

		return promotionRepository.findActivePromotionDetailById(userId, promotionId, issuedAt, active).orElseThrow(
			()-> new ParkingEasyException(PromotionErrorCode.PROMOTION_NOT_FOUND)
		);

	}
}
