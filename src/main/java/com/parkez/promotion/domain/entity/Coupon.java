package com.parkez.promotion.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.parkez.common.entity.BaseEntity;
import com.parkez.promotion.domain.enums.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon")
public class Coupon extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DiscountType discountType;

	@Column(nullable = false)
	private Integer discountValue;

	@Lob
	private String description;

	@Builder
	private Coupon(String name, DiscountType discountType, Integer discountValue, String description) {
		this.name = name;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.description = description;
	}

	public BigDecimal calculateDiscount(BigDecimal originalPrice) {
		return switch (this.discountType) {
			case FIXED -> originalPrice.min(BigDecimal.valueOf(discountValue));
			case PERCENT -> originalPrice.multiply(BigDecimal.valueOf(discountValue))
				.divide(BigDecimal.valueOf(100),RoundingMode.FLOOR);
		};
	}

}
