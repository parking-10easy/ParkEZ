package com.parkez.promotion.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionStatus {

	ACTIVE("활성화"),
	ENDED("종료")
	;

	private final String description;

}
