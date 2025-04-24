package com.parkez.promotion.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetailProjection;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	@Query(
		value = """
			select
				p.id as id,
			 	p.name as promotionName,
			  	p.promotionType as promotionType,
			   	p.limitPerUser as limitPerUser,
				p.promotionStartAt as promotionStartAt,
				p.promotionEndAt as promotionEndAt,
				c.discountValue as discountValue,
				c.name as couponName
			from
				Promotion p join p.coupon c
			where
				p.promotionStatus = :promotionStatus
				and p.promotionStartAt <= :now
				and p.promotionEndAt >= :now
			order by p.promotionEndAt asc, p.promotionStartAt desc
			""",
		countQuery = """
				select count(p)
				from Promotion p
				where
					p.promotionStatus = :promotionStatus
					and p.promotionStartAt <= :now
					and p.promotionEndAt >= :now
			"""
	)
	Page<ActivePromotionProjection> findActivePromotions(LocalDateTime now, PromotionStatus promotionStatus,
		Pageable pageable);

	@Query(value = """
		select
			p.id,
		  	p.name as promotionName,
		  	p.promotion_type,
		  	p.promotion_start_at,
		  	p.promotion_end_at,
		  	p.valid_days_after_issue,
		  	p.limit_total,
		  	p.limit_per_user,
		  	c.name as couponName,
		  	c.discount_value,
		  	case
			  	when count(
						   	case when pi.user_id = :userId then 1 end) < p.limit_per_user then 'true'
				else 'false' end                                   							as isAvailableToIssue,
		  	p.limit_total - count(pi.id)                           							as remainingQuantity,
			p.limit_per_user - count(case when pi.user_id = :userId then 1 end) 			as availableIssueCount,
			CAST(DATE_ADD(:issuedAt, INTERVAL p.valid_days_after_issue DAY) as DATETIME)	as expiresAtIfIssuedNow
		from promotion p
			join coupon c on p.coupon_id = c.id
			left join promotion_issue pi on p.id = pi.promotion_id
		where
			p.id = :promotionId
			and p.promotion_status = :activeStatus
			and p.promotion_start_at <= :issuedAt
			and p.promotion_end_at >= :issuedAt
		group by
			p.id,
			p.name,
			p.promotion_type,
			p.promotion_start_at,
			p.promotion_end_at,
			p.valid_days_after_issue,
			p.limit_total,
			p.limit_per_user,
			c.name,
			c.discount_value
		""", nativeQuery = true)
	Optional<PromotionDetailProjection> findActivePromotionDetailById(Long userId, Long promotionId, LocalDateTime issuedAt, String activeStatus);
}
