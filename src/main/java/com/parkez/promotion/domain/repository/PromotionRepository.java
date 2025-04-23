package com.parkez.promotion.domain.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.repository.projection.ActivePromotionProjection;

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
}
