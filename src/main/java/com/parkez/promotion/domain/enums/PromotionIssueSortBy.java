package com.parkez.promotion.domain.enums;

import java.util.Arrays;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.promotion.excption.PromotionErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionIssueSortBy {

	ISSUED_AT,
	EXPIRES_AT;

	public static PromotionIssueSortBy from(String source) {
		return Arrays.stream(values())
			.filter(promotionIssueSortBy -> promotionIssueSortBy.name().equalsIgnoreCase(source))
			.findFirst()
			.orElseThrow(()-> new ParkingEasyException(PromotionErrorCode.INVALID_SORT_BY));
	}

}
