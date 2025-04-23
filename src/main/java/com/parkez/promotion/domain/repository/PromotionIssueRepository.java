package com.parkez.promotion.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.promotion.domain.entity.PromotionIssue;

public interface PromotionIssueRepository extends JpaRepository<PromotionIssue, Long> {
}
