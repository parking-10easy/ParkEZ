package com.parkez.promotion.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.parkez.common.entity.BaseEntity;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.user.domain.entity.User;

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
@Table(name = "promotion_issue")
public class PromotionIssue extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id")
	private Promotion promotion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private LocalDateTime issuedAt;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private LocalDateTime usedAt;

	@Enumerated(EnumType.STRING)
	private PromotionIssueStatus status;

	@Builder
	private PromotionIssue(Promotion promotion, User user, LocalDateTime issuedAt, LocalDateTime expiresAt) {
		this.promotion = promotion;
		this.user = user;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.status = PromotionIssueStatus.ISSUED;
	}

	public boolean isUsed() {
		return !Objects.isNull(this.usedAt);
	}

	public boolean isExpired(LocalDateTime now) {
		return this.expiresAt.isBefore(now);
	}

	public Coupon getCoupon() {
		return this.promotion.getCoupon();
	}

	public void use(LocalDateTime now) {
		this.usedAt = now;
		this.status = PromotionIssueStatus.USED;
	}

	public void cancelUsage() {
		this.usedAt = null;
		this.status = PromotionIssueStatus.ISSUED;
	}

	public boolean isOwnedBy(Long userId) {
		return Objects.equals(this.user.getId(), userId);
	}

}
