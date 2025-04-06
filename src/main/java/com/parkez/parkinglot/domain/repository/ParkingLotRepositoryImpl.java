package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.parkez.parkinglot.domain.entity.QParkingLot.parkingLot;

@Repository
@RequiredArgsConstructor
public class ParkingLotRepositoryImpl implements ParkingLotQueryRepository {

    private final JPAQueryFactory jqf;

    // 다건 조회
    @Override
    public Page<ParkingLot> searchParkingLots(String name, String address, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(name)) {
            builder.and(parkingLot.name.contains(name));
        }
        if (StringUtils.hasText(address)) {
            builder.and(parkingLot.address.contains(address));
        }
        List<ParkingLot> parkingLots = jqf
                .selectFrom(parkingLot)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jqf.select(parkingLot.count()).from(parkingLot).where(builder).fetchOne();

        return new PageImpl<>(parkingLots, pageable, total != null ? total : 0);
    }

    // 단건 조회
    @Override
    public ParkingLot findParkingLotById(Long parkingLotId) {
        return jqf
                .selectFrom(parkingLot)
                .where(parkingLot.id.eq(parkingLotId))
                .fetchOne();
    }

}
