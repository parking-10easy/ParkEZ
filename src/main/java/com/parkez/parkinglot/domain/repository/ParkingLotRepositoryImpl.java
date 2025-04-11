package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    //TODO
    @Override
    public Page<ParkingLot> searchParkingLotsByConditions(String name, String address, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(nameContains(name));
        builder.and(addressContains(address));
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
    // TODO : 리뷰 + 썸네일 이미지
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

        JPAQuery<Long>  countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .from(parkingLot)
                .where(builder);

        return PageableExecutionUtils.getPage(myParkingLots, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? parkingLot.name.contains(name) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? parkingLot.address.contains(address) : null;
    }

}
