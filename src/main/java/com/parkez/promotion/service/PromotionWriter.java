package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.domain.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PromotionWriter {

	private final PromotionRepository promotionRepository;

	public Promotion create(Coupon coupon, String name, PromotionType promotionType, Integer limitTotal,
		Integer limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt,
		Integer validDaysAfterIssue) {

		Promotion promotion = Promotion.builder()
			.name(name)
			.promotionType(promotionType)
			.coupon(coupon)
			.limitTotal(limitTotal)
			.limitPerUser(limitPerUser)
			.promotionStartAt(promotionStartAt)
			.promotionEndAt(promotionEndAt)
			.validDaysAfterIssue(validDaysAfterIssue)
			.build();

		return promotionRepository.save(promotion);
	}

	public int expireEndedPromotions(LocalDateTime currentDateTime, PromotionStatus currentStatus,
		PromotionStatus targetStatus) {
		return promotionRepository.bulkUpdatePromotionStatusToEndedByCurrentDateTime(currentDateTime, currentStatus, targetStatus);
	}

	public int expireSoldOutPromotions(PromotionStatus currentStatus, PromotionStatus targetStatus) {

		return promotionRepository.bulkUpdatePromotionStatusToEndedIfSoldOut(currentStatus, targetStatus);
	}

}
