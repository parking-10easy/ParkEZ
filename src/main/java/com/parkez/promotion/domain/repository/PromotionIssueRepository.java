package com.parkez.promotion.domain.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;

public interface PromotionIssueRepository extends JpaRepository<PromotionIssue, Long>, PromotionQueryDslRepository {

	int countByPromotionId(@Param("promotionId") Long promotionId);

	int countByPromotionIdAndUserId(@Param("promotionId") Long promotionId, @Param("userId") Long userId);

	@Modifying
	@Query("""
		update PromotionIssue pi
		set
			pi.status = :targetStatus
		where
			pi.expiresAt <= :currentDateTime
			and pi.status = :currentStatus
	""")
	int bulkUpdateStatusByCurrentTime(
		@Param("currentDateTime") LocalDateTime currentDateTime,
		@Param("currentStatus") PromotionIssueStatus currentStatus,
		@Param("targetStatus") PromotionIssueStatus targetStatus);
}
