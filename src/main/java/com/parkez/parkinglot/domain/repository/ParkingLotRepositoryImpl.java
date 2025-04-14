package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;
import static com.parkez.parkingzone.domain.entity.QParkingZone.parkingZone;

@Repository
@RequiredArgsConstructor
public class ParkingLotRepositoryImpl implements ParkingLotQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 다건 조회
    //TODO : 리뷰 카운트 + 평점 + 정렬 기준
    @Override
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address,
                                                                        Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(nameContains(name));
        builder.and(addressContains(address));
        builder.and(withinRadius(userLatitude, userLongitude, radiusInMeters));
        builder.and(parkingLot.address.isNotEmpty()); // TODO : 주소값이 비어있는 경우, 제외
        builder.and(notDeleted());

        // 조건에 맞는 엔티티 조회
        List<ParkingLot> parkingLots = jpaQueryFactory
                .selectFrom(parkingLot)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 조회한 엔티티를 Dto로 반환
        List<ParkingLotSearchResponse> dtoList = parkingLots.stream()
                .map(ParkingLotSearchResponse::from)
                .toList();

        // availableQuantity 계산
        Map<Long, Long> countZoneMap = countAvailableQuantity(parkingLots);

        // DTO 후처리
        for (ParkingLotSearchResponse dto : dtoList) {
            Long availableQuantity = countZoneMap.getOrDefault(dto.getParkingLotId(), 0L);
            dto.updateAvailableQuantity(availableQuantity);
        }

        // page 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(parkingLot.count())
                .from(parkingLot)
                .where(builder);

        return PageableExecutionUtils.getPage(dtoList, pageable, countQuery::fetchOne);
    }

    // 단건 조회
    // TODO : 리뷰 카운트 + 평점
    @Override
    public Optional<ParkingLotSearchResponse> searchParkingLotById(Long parkingLotId) {
        ParkingLot parkingLotEntity = jpaQueryFactory.selectFrom(parkingLot)
                .where(parkingLot.id.eq(parkingLotId)
                        .and(notDeleted()))
                .fetchOne();

        if (parkingLotEntity == null) {
            return Optional.empty();
        }

        // 조회한 엔티티를 Dto로 반환
        ParkingLotSearchResponse responseDto = ParkingLotSearchResponse.from(parkingLotEntity);

        // availableQuantity 계산
        Long availableQuantity = countAvailableQuantity(parkingLotId);

        // DTO 후처리
        responseDto.updateAvailableQuantity(availableQuantity);

        return Optional.of(responseDto);
    }

    // 소유한 주차장 조회
    // TODO : 리뷰 카운트 + 정렬 기준
    @Override
    public Page<ParkingLot> findMyParkingLots(Long userId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(parkingLot.owner.id.eq(userId));
        builder.and(notDeleted());

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


    // (다건 조회) availableQuantity 계산
    private Map<Long, Long> countAvailableQuantity(List<ParkingLot> parkingLots) {
        List<Long> parkingLotIds = parkingLots.stream()
                .map(ParkingLot::getId)
                .toList();

        List<Tuple> countZoneTuples = jpaQueryFactory
                .select(parkingZone.parkingLot.id, parkingZone.count())
                .from(parkingZone)
                .where(parkingZone.parkingLot.id.in(parkingLotIds)
                        .and(parkingZone.deletedAt.isNull()))
                .groupBy(parkingZone.parkingLot.id)
                .fetch();

        return countZoneTuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(parkingZone.parkingLot.id),
                        tuple -> tuple.get(parkingZone.count())
                ));
    }

    // (단건 조회) availableQuantity 계산
    private Long countAvailableQuantity(Long parkingLotId) {
        Long count = jpaQueryFactory.select(parkingZone.count())
                .from(parkingZone)
                .where(
                        parkingZone.parkingLot.id.eq(parkingLotId)
                                .and(parkingZone.deletedAt.isNull()))
                .fetchOne();
        return count != null ? count : 0L;
    }

}
