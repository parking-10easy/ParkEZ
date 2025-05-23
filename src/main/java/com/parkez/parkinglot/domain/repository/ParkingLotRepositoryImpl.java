package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.dto.aggregation.ParkingLotAggregation;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;
import static com.parkez.parkinglot.domain.entity.QParkingLotImage.parkingLotImage;
import static com.parkez.parkingzone.domain.entity.QParkingZone.parkingZone;
import static com.parkez.reservation.domain.entity.QReservation.reservation;
import static com.parkez.review.domain.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ParkingLotRepositoryImpl implements ParkingLotQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 다건 조회
    @Override
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address,
                                                                        Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable) {

        List<ParkingLotSearchResponse> dtoList = jpaQueryFactory
                .select(Projections.constructor(ParkingLotSearchResponse.class,
                        parkingLot.id.min(),
                        parkingLot.name,
                        parkingLot.address,
                        parkingLot.openedAt,
                        parkingLot.closedAt,
                        parkingLot.pricePerHour,
                        parkingLot.quantity.as("totalQuantity"),
                        parkingLot.chargeType,
                        parkingLot.sourceType,
                        parkingLot.status.as("parkingLotStatus")
                ))
                .from(parkingLot)
                .where(
                        nameContains(name),
                        addressContains(address),
                        withinRadius(userLatitude, userLongitude, radiusInMeters),
                        parkingLot.address.isNotEmpty(),
                        notDeleted()
                )
                .groupBy(
                        parkingLot.name,
                        parkingLot.address,
                        parkingLot.openedAt,
                        parkingLot.closedAt,
                        parkingLot.pricePerHour,
                        parkingLot.quantity,
                        parkingLot.chargeType,
                        parkingLot.sourceType,
                        parkingLot.status
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // page 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .distinct()
                .from(parkingLot)
                .where(
                        nameContains(name),
                        addressContains(address),
                        withinRadius(userLatitude, userLongitude, radiusInMeters),
                        parkingLot.address.isNotEmpty(),
                        notDeleted()
                );

        return PageableExecutionUtils.getPage(dtoList, pageable, countQuery::fetchOne);
    }

    // 단건 조회
    @Override
    public ParkingLotSearchResponse searchParkingLotById(Long parkingLotId) {

        return jpaQueryFactory
                .select(Projections.constructor(ParkingLotSearchResponse.class,
                        parkingLot.id,
                        parkingLot.name,
                        parkingLot.address,
                        parkingLot.openedAt,
                        parkingLot.closedAt,
                        parkingLot.pricePerHour,
                        parkingLot.quantity.as("totalQuantity"),
                        parkingLot.chargeType,
                        parkingLot.sourceType,
                        parkingLot.status.as("parkingLotStatus")
                ))
                .from(parkingLot)
                .where(
                        parkingLot.id.eq(parkingLotId),
                        parkingLot.address.isNotEmpty(),
                        notDeleted()
                )
                .groupBy(parkingLot.id)
                .fetchOne();
    }

    // 소유한 주차장 조회
    @Override
    public Page<MyParkingLotSearchResponse> findMyParkingLots(Long userId, Pageable pageable) {

        List<MyParkingLotSearchResponse> myParkingLots = jpaQueryFactory
                .select(Projections.constructor(MyParkingLotSearchResponse.class,
                        parkingLot.id,
                        parkingLot.name,
                        parkingLot.address

                ))
                .from(parkingLot)
                .where(
                        parkingLot.owner.id.eq(userId),
                        notDeleted()
                )
                .groupBy(
                        parkingLot.id
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // page 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .distinct()
                .from(parkingLot)
                .where(
                        parkingLot.owner.id.eq(userId),
                        notDeleted()
                );

        return PageableExecutionUtils.getPage(myParkingLots, pageable, countQuery::fetchOne);
    }

    // 이미지 리스트 찾기
    @Override
    public List<String> findImageListByParkingLotId(Long parkingLotId) {
        return jpaQueryFactory
                .select(parkingLotImage.imageUrl)
                .from(parkingLotImage)
                .where(parkingLotImage.parkingLot.id.eq(parkingLotId))
                .fetch();
    }

    // 주차장 조회시 필요한 집계 쿼리
    @Override
    public Optional<ParkingLotAggregation> getAggregationByParkingLotId(Long parkingLotId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                        Projections.constructor(
                                ParkingLotAggregation.class,
                                parkingLot.id,
                                parkingZone.countDistinct(),
                                review.count(),
                                review.rating.avg()
                        ))
                .from(parkingLot)
                .leftJoin(parkingZone).on(parkingZone.parkingLot.id.eq(parkingLot.id))
                .leftJoin(reservation).on(reservation.parkingZone.id.eq(parkingZone.id))
                .leftJoin(review).on(review.reservation.id.eq(reservation.id))
                .where(parkingLot.id.eq(parkingLotId))
                .groupBy(parkingLot.id)
                .fetchOne());
    }

    // 이름 조건
    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? parkingLot.name.contains(name) : null;
    }

    // 주소 조건
    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? parkingLot.address.contains(address) : null;
    }

    // 거리 범위 조건
    private BooleanExpression withinRadius(Double userLatitude, Double userLongitude, Integer radiusInMeters) {
        if (userLatitude == null || userLongitude == null || radiusInMeters == null) {
            return null;
        }
        // 거리 계산
        NumberExpression<Double> distance = Expressions.numberTemplate(
                Double.class,
                "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3}))",
                parkingLot.longitude, parkingLot.latitude,
                userLongitude, userLatitude
        );
        return distance.loe(radiusInMeters);
    }

    // 삭제x 조건
    private BooleanExpression notDeleted() {
        return parkingLot.deletedAt.isNull();
    }

}
