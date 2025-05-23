package com.parkez.promotion.domain.enums;

import java.util.Arrays;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.excption.PromotionErrorCode;
import com.parkez.promotion.excption.PromotionIssueErrorCode;

import lombok.Getter;

@Getter
public enum PromotionIssueStatus {

	ISSUED,
	USED,
	EXPIRED;

	public static PromotionIssueStatus from(String source) {
		return Arrays.stream(values())
			.filter(promotionIssueStatus -> promotionIssueStatus.name().equalsIgnoreCase(source))
			.findFirst()
			.orElseThrow(()-> new ParkingEasyException(PromotionIssueErrorCode.INVALID_ISSUE_STATUS));
	}

}
