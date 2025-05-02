package com.parkez.promotion.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.parkez.promotion.domain.enums.PromotionIssueSortBy;

@Component
public class PromotionIssueSortByConverter implements Converter<String, PromotionIssueSortBy> {
	@Override
	public PromotionIssueSortBy convert(String source) {
		return PromotionIssueSortBy.from(source);
	}
}
