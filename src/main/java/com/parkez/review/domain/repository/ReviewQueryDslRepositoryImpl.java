package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import com.parkez.review.enums.ReviewSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                .where(
                        parkingLot.id.eq(parkingLotId),
                        parkingLot.deletedAt.isNull()
                )
                .orderBy(getSortOrder(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(review.count())
                .from(review)
                .join(review.reservation, reservation)
                .join(reservation.parkingZone, parkingZone)
                .join(parkingZone.parkingLot, parkingLot)
                .where(
                        parkingLot.id.eq(parkingLotId),
                        parkingLot.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private OrderSpecifier<?> getSortOrder(ReviewSortType sortType) {

        if (sortType == null) {
            return review.createdAt.desc();
        }

        return switch (sortType) {
            case RATING_DESC -> review.rating.desc();
            case RATING_ASC -> review.rating.asc();
            default -> review.createdAt.desc();
        };
    }
}
