package com.parkez.promotion.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parkez.promotion.domain.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
