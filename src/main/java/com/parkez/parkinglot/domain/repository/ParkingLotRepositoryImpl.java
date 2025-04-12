package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.querydsl.core.BooleanBuilder;
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

@Repository
@RequiredArgsConstructor
public class ParkingLotRepositoryImpl implements ParkingLotQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 다건 조회
    //TODO : 리뷰 카운트 + 별점 + availableQuantity 추가
    @Override
    public Page<ParkingLot> searchParkingLotsByConditions(String name, String address,
                                                          Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(nameContains(name));
        builder.and(addressContains(address));
        builder.and(withinRadius(userLatitude, userLongitude, radiusInMeters));
        builder.and(parkingLot.address.isNotEmpty()); // TODO : 주소값이 비어있는 경우, 제외
        builder.and(parkingLot.deletedAt.isNull());
        List<ParkingLot> parkingLots = jpaQueryFactory
                .selectFrom(parkingLot)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .from(parkingLot)
                .where(builder);

        return PageableExecutionUtils.getPage(parkingLots, pageable, countQuery::fetchOne);
    }

    // 단건 조회
    // TODO
    @Override
    public Optional<ParkingLot> searchParkingLotById(Long parkingLotId) {
        return Optional.ofNullable(
                jpaQueryFactory.selectFrom(parkingLot)
                        .where(parkingLot.id.eq(parkingLotId)
                                .and(parkingLot.deletedAt.isNull()))
                        .fetchOne());
    }

    // 소유한 주차장 조회
    // TODO : 리뷰 카운트
    @Override
    public Page<ParkingLot> findMyParkingLots(Long userId, Pageable pageable) {
        BooleanExpression ownerMatches = parkingLot.owner.id.eq(userId);
        BooleanExpression notDeleted = parkingLot.deletedAt.isNull();
        BooleanBuilder builder = new BooleanBuilder(ownerMatches).and(notDeleted);

        List<ParkingLot> myParkingLots = jpaQueryFactory
                .selectFrom(parkingLot)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .from(parkingLot)
                .where(builder);

        return PageableExecutionUtils.getPage(myParkingLots, pageable, countQuery::fetchOne);
    }

    // 이름 조건
    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? parkingLot.name.contains(name) : null;
    }

    // 주소 조건
    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? parkingLot.address.contains(address) : null;
    }

    // 범위 조건
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

}
