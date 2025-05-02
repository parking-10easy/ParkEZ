package com.parkez.promotion.domain.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;
import com.parkez.promotion.domain.repository.projection.PromotionDetail;

import jakarta.persistence.LockModeType;

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
	Page<ActivePromotionProjection> findActivePromotions(@Param("now") LocalDateTime now,@Param("promotionStatus") PromotionStatus promotionStatus,
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
			  	when count(case when pi.user_id = :userId then 1 end) < p.limit_per_user
				then 'true'	else 'false' end                                   				as isAvailableToIssue,
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
	Optional<PromotionDetail> findActivePromotionDetail(@Param("userId") Long userId,@Param("promotionId") Long promotionId,@Param("issuedAt") LocalDateTime issuedAt, @Param("activeStatus") String activeStatus);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = """
		select p
		from Promotion p join fetch p.coupon
		where
			p.id = :promotionId
			and p.promotionStatus = :activeStatus
			and p.promotionStartAt <= :now
			and p.promotionEndAt >= :now
	""")
	Optional<Promotion> findActivePromotionWithPessimisticLock(@Param("promotionId") Long promotionId, @Param("now") LocalDateTime now, @Param("activeStatus") PromotionStatus activeStatus);

	@Modifying
	@Query("""
		update Promotion p
		set p.promotionStatus = :targetStatus
		where
			p.promotionEndAt <= :currentDateTime
			and p.promotionStatus = :currentStatus
	""")
	int bulkUpdatePromotionStatusToEndedByCurrentDateTime(@Param("currentDateTime") LocalDateTime currentDateTime,@Param("currentStatus") PromotionStatus currentStatus,@Param("targetStatus") PromotionStatus targetStatus);

	@Modifying
	@Query("""
		update Promotion p
		set p.promotionStatus = :targetStatus
		where
			p.limitTotal <= (
				select count(pi)
				from PromotionIssue pi
				where pi.promotion.id = p.id
			)
			and p.promotionStatus = :currentStatus
	""")
	int bulkUpdatePromotionStatusToEndedIfSoldOut(@Param("currentStatus") PromotionStatus currentStatus,@Param("targetStatus") PromotionStatus targetStatus);
}
