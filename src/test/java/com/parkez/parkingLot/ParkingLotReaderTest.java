package com.parkez.parkingLot;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotReaderTest {

    @InjectMocks
    private ParkingLotReader parkingLotReader;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    private ParkingLot parkingLot1;
    private ParkingLot parkingLot2;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        // Dummy User 생성 (필요한 최소 정보만 설정)
        dummyUser = User.builder()
                .email("owner@example.com")
                .nickname("Owner")
                .phone("010-1234-5678")
                .role(UserRole.ROLE_OWNER)
                .build();

        // parkingLot1 생성
        parkingLot1 = ParkingLot.builder()
                .owner(dummyUser)
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 123")
                .openedAt(LocalTime.of(9, 0))
                .closedAt(LocalTime.of(22, 0))
                .pricePerHour(new BigDecimal("2000"))
                .description("설명 1")
                .quantity(100)
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.OWNER_REGISTERED) // 소유자 등록 데이터
                .build();

        // parkingLot2 생성
        parkingLot2 = ParkingLot.builder()
                .owner(dummyUser)
                .name("어려운주차장")
                .address("서울시 강남구 테헤란로 111")
                .openedAt(LocalTime.of(9, 0))
                .closedAt(LocalTime.of(22, 0))
                .pricePerHour(new BigDecimal("12000"))
                .description("설명 2")
                .quantity(200)
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.PUBLIC_DATA) // 공공 데이터
                .build();
    }

    @Test
    void 필터_조건이_없을_때_주차장을_전체_조회한다() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ParkingLot> parkingLotList = Arrays.asList(parkingLot1, parkingLot2);
        Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

        when(parkingLotRepository.searchParkingLots(null, null, pageable)).thenReturn(page);

        Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLots(null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("참쉬운주차장", result.getContent().get(0).getName());
        assertEquals("어려운주차장", result.getContent().get(1).getName());
    }

    @Test
    void 이름으로_주차장을_조회한다() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ParkingLot> parkingLotList = Arrays.asList(parkingLot1);
        Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

        when(parkingLotRepository.searchParkingLots("참쉬운", null, pageable)).thenReturn(page);

        Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLots("참쉬운", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("참쉬운주차장", result.getContent().get(0).getName());
    }

    @Test
    void 주소로_주차장을_조회한다() {
        Pageable pageable = PageRequest.of(0, 10);
        // 주소에 "테헤란로 111"이 포함된 경우 parkingLot2만 반환
        List<ParkingLot> parkingLotList = Arrays.asList(parkingLot2);
        Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

        when(parkingLotRepository.searchParkingLots(null, "테헤란로 111", pageable)).thenReturn(page);

        Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLots(null, "테헤란로 111", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("어려운주차장", result.getContent().get(0).getName());
    }
}
