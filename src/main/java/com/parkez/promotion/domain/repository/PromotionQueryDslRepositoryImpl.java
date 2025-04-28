package com.parkez.promotion.domain.repository;

import static com.parkez.promotion.domain.entity.QCoupon.*;
import static com.parkez.promotion.domain.entity.QPromotion.*;
import static com.parkez.promotion.domain.entity.QPromotionIssue.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.parkez.promotion.domain.enums.PromotionIssueSortBy;
import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.dto.response.MyCouponResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PromotionQueryDslRepositoryImpl implements PromotionQueryDslRepository{

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<MyCouponResponse> findMyCouponsByStatus(Long userId, PromotionIssueStatus status, Pageable pageable,
		PromotionIssueSortBy sortBy, Direction direction) {

		List<MyCouponResponse> content = queryFactory
			.select(Projections.constructor(
				MyCouponResponse.class,
				promotion.id,
				promotion.name,
				coupon.name,
				coupon.discountValue,
				coupon.discountType,
				promotionIssue.issuedAt,
				promotionIssue.expiresAt,
				promotionIssue.usedAt
			))
			.from(promotionIssue)
			.join(promotionIssue.promotion, promotion)
			.join(promotion.coupon, coupon)
			.where(
				userIdEq(userId),
				statusEq(status)
			)
			.orderBy(getOrderSpecifier(direction, sortBy))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = queryFactory
			.select(promotionIssue.count())
			.from(promotionIssue)
			.where(
				userIdEq(userId),
				statusEq(status)
			);
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	private BooleanExpression userIdEq(Long userId) {
		return promotionIssue.user.id.eq(userId);
	}

	private BooleanExpression statusEq(PromotionIssueStatus status) {
		return status != null ? promotionIssue.status.eq(status) : null;
	}

	private OrderSpecifier<?> getOrderSpecifier(Direction direction, PromotionIssueSortBy sortBy) {
		Order order = direction == Direction.ASC ? Order.ASC : Order.DESC;
		return switch (sortBy) {
			case ISSUED_AT -> new OrderSpecifier<>(order, promotionIssue.issuedAt);
			case EXPIRES_AT -> new OrderSpecifier<>(order, promotionIssue.expiresAt);
		};
	}

}
