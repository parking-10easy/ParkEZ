package com.parkez.promotion.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.parkez.promotion.domain.enums.PromotionIssueStatus;

@Component
public class PromotionIssueStatusConverter implements Converter<String, PromotionIssueStatus> {
	@Override
	public PromotionIssueStatus convert(String source) {
		return PromotionIssueStatus.from(source);
	}
}
