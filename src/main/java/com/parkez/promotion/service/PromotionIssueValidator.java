package com.parkez.promotion.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.excption.PromotionIssueErrorCode;

@Component
public class PromotionIssueValidator {

	public void validateCanBeUsed(PromotionIssue promotionIssue, LocalDateTime now) {

		if (promotionIssue.isUsed()) {
			throw new ParkingEasyException(PromotionIssueErrorCode.ALREADY_USED);
		}

		if (promotionIssue.isExpired(now)) {
			throw new ParkingEasyException(PromotionIssueErrorCode.EXPIRED_COUPON);
		}

	}
}
