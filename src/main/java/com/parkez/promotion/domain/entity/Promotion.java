package com.parkez.promotion.domain.entity;

import java.time.LocalDateTime;

import com.parkez.common.entity.BaseEntity;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "promotion")
public class Promotion extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PromotionType promotionType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@Column(nullable = false)
	private Integer limitTotal;

	@Column(nullable = false)
	private Integer limitPerUser;

	@Column(nullable = false)
	private LocalDateTime promotionStartAt;

	@Column(nullable = false)
	private LocalDateTime promotionEndAt;

	@Column(nullable = false)
	private Integer validDaysAfterIssue;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PromotionStatus promotionStatus;

	@Builder
	private Promotion(String name, PromotionType promotionType, Coupon coupon, Integer limitTotal, Integer limitPerUser,
		LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, Integer validDaysAfterIssue) {
		this.name = name;
		this.promotionType = promotionType;
		this.coupon = coupon;
		this.limitTotal = limitTotal;
		this.limitPerUser = limitPerUser;
		this.promotionStartAt = promotionStartAt;
		this.promotionEndAt = promotionEndAt;
		this.validDaysAfterIssue = validDaysAfterIssue;
		this.promotionStatus = PromotionStatus.ACTIVE;
	}

	public void updateStatus(PromotionStatus promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public boolean hasRemainingQuantity(int issuedCount) {
		return issuedCount < this.limitTotal;
	}

	public boolean canUserIssueMore(int userIssuedCount) {
		return userIssuedCount < this.limitPerUser;
	}

	public Long getCouponId() {
		return this.coupon.getId();
	}

	public String getCouponName() {
		return this.coupon.getName();
	}
}
