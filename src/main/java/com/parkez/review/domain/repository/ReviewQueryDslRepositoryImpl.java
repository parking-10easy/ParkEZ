package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import com.parkez.review.enums.ReviewSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;
import static com.parkez.parkingzone.domain.entity.QParkingZone.parkingZone;
import static com.parkez.reservation.domain.entity.QReservation.reservation;
import static com.parkez.review.domain.entity.QReview.review;
import static com.parkez.user.domain.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReviewQueryDslRepositoryImpl implements ReviewQueryDslRepository{

    private final JPAQueryFactory queryFactory;

    public Page<Review> findAllByParkingLotIdWithSort(Long parkingLotId, Pageable pageable, ReviewSortType sortType) {

        List<Review> content = queryFactory
                .selectFrom(review)
                .join(review.reservation, reservation)
                .join(reservation.parkingZone, parkingZone)
                .join(parkingZone.parkingLot, parkingLot)
                .join(review.user, user).fetchJoin()
                .where(isActiveParkingLot(parkingLotId))
                .orderBy(getSortOrder(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .join(review.reservation, reservation)
                .join(reservation.parkingZone, parkingZone)
                .join(parkingZone.parkingLot, parkingLot)
                .where(isActiveParkingLot(parkingLotId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression isActiveParkingLot(Long id) {
        return parkingLot.id.eq(id)
                .and(parkingLot.deletedAt.isNull());
    }

    private OrderSpecifier<?> getSortOrder(ReviewSortType sortType) {

        return switch (sortType) {
            case RATING_DESC -> review.rating.desc();
            case RATING_ASC -> review.rating.asc();
            default -> review.createdAt.desc();
        };
    }
}
