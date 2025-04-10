package com.parkez.parkingLot;

import com.parkez.common.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotReaderTest {

    @InjectMocks
    private ParkingLotReader parkingLotReader;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    private final PageRequest pageRequest = new PageRequest(1, 10);
    Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize());

    private User getOwnerUser() {
        User ownerUser = User.builder()
                .email("owner@example.com")
                .nickname("Owner")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(ownerUser, "id", 1L);
        return ownerUser;
    }

    private ParkingLot getParkingLot1() {
        User ownerUser = getOwnerUser();
        return ParkingLot.builder()
                .owner(ownerUser)
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 123")
                .images(new ArrayList<>())
                .build();
    }

    private ParkingLot getParkingLot2() {
        User ownerUser = getOwnerUser();
        return ParkingLot.builder()
                .owner(ownerUser)
                .name("어려운주차장")
                .address("서울시 강남구 테헤란로 111")
                .images(new ArrayList<>())
                .build();
    }

    @Nested
    class searchParkingLotsByConditions {
        @Test
        void 검색_조건이_없을_때_주차장을_전체_조회한다() {
            // given
            ParkingLot parkingLot1 = getParkingLot1();
            ParkingLot parkingLot2 = getParkingLot2();
            List<ParkingLot> parkingLotList = Arrays.asList(parkingLot1, parkingLot2);
            Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            // when
            when(parkingLotRepository.searchParkingLotsByConditions(null, null, pageable)).thenReturn(page);

            // then
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(null, null,  pageRequest.getPage(),pageRequest.getSize());
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress()),
                            tuple(parkingLot2.getName(), parkingLot2.getAddress())
                    );
        }

        @Test
        void 이름으로_주차장을_조회한다() {
            // given
            ParkingLot parkingLot1 = getParkingLot1();
            List<ParkingLot> parkingLotList = Arrays.asList(parkingLot1);
            Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            // when
            String name = parkingLot1.getName();
            when(parkingLotRepository.searchParkingLotsByConditions(name, null, pageable)).thenReturn(page);

            // then
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(name, null, pageRequest.getPage(),pageRequest.getSize());
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress())
                    );
        }

        @Test
        void 주소로_주차장을_조회한다() {
            // given
            ParkingLot parkingLot2 = getParkingLot2();
            List<ParkingLot> parkingLotList = Arrays.asList(parkingLot2);
            Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            // when
            String address = parkingLot2.getAddress();
            when(parkingLotRepository.searchParkingLotsByConditions(null, address, pageable)).thenReturn(page);

            // then
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(null, address,  pageRequest.getPage(),pageRequest.getSize());
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(parkingLot2.getName(), parkingLot2.getAddress())
                    );
        }

        @Test
        void 이름과_주소로_주차장을_조회한다() {
            // given
            ParkingLot parkingLot2 = getParkingLot2();
            List<ParkingLot> parkingLotList = Arrays.asList(parkingLot2);
            Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            // when
            String name = parkingLot2.getName();
            String address = parkingLot2.getAddress();
            when(parkingLotRepository.searchParkingLotsByConditions(name, address, pageable)).thenReturn(page);

            // then

            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(name, address,  pageRequest.getPage(),pageRequest.getSize());
            assertNotNull(result);
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(parkingLot2.getName(), parkingLot2.getAddress())
                    );
        }

    }

    @Nested
    class searchParkingLotById {
        @Test
        void 아이디로_주차장을_단건_조회한다() {
            // given
            Long parkingLotId = 1L;
            ParkingLot parkingLot1 = getParkingLot1();
            when(parkingLotRepository.searchParkingLotById(parkingLotId)).thenReturn(Optional.ofNullable(parkingLot1));

            // when
            ParkingLot found = parkingLotReader.searchParkingLotById(parkingLotId);

            // then
            assertEquals("참쉬운주차장", found.getName());

        }

        @Test
        void 유효하지_않은_아이디로_주차장_단건_조회에_실패한다() {
            // given
            Long parkingLotId = -1L;
            when(parkingLotRepository.searchParkingLotById(parkingLotId)).thenReturn(Optional.empty());

            // when
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () -> {
                parkingLotReader.searchParkingLotById(parkingLotId);
            });

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class getMyParkingLots {
        @Test
        void 본인이_소유한_주차장을_조회한다() {
            //given
            ParkingLot parkingLot1 = getParkingLot1();
            ParkingLot parkingLot2 = getParkingLot2();
            List<ParkingLot> parkingLotList = Arrays.asList(parkingLot1, parkingLot2);
            Page<ParkingLot> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());
            Long userId = 1L;
            when(parkingLotRepository.findMyParkingLots(userId, pageable)).thenReturn(page);

            // when
            Page<MyParkingLotSearchResponse> result = parkingLotReader.getMyParkingLots(userId,  pageRequest.getPage(),pageRequest.getSize());

            //then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress()),
                            tuple(parkingLot2.getName(), parkingLot2.getAddress())
                    );
        }

        @Test
        void 본인이_등록하지_않은_주차장은_조회되지_않는다() {
            // given
            Long nonParkingLotOwnerId = 2L;
            Page<ParkingLot> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
            when(parkingLotRepository.findMyParkingLots(nonParkingLotOwnerId, pageable)).thenReturn(emptyPage);

            // when
            Page<MyParkingLotSearchResponse> result = parkingLotReader.getMyParkingLots(nonParkingLotOwnerId,  pageRequest.getPage(),pageRequest.getSize());

            // then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }


    @Nested
    class getOwnedParkingLot {

        @Test
        void 아이디로_주차장_엔티티를_조회한다() {
            // given
            Long parkingLotId = 1L;
            ParkingLot parkingLot1 = getParkingLot1();
            when(parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId))
                    .thenReturn(Optional.of(parkingLot1));

            // when
            Long userId = 1L;
            ParkingLot result = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);

            // then
            assertNotNull(result);
            assertEquals(parkingLot1.getName(), result.getName());
        }

        @Test
        void 유효하지_않은_아이디로_주차장_엔티티_조회에_실패한다() {
            // given
            Long parkingLotId = -1L;
            when(parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId))
                    .thenReturn(Optional.empty());

            // when
            Long userId = 1L;
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () -> {
                parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
            });

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void softDelete된_주차장_엔티티_조회시_예외를_발생시킨다() {
            // given
            Long parkingLotId = 1L;
            when(parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId))
                    .thenReturn(Optional.empty());

            // when
            Long userId = 1L;
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotReader.getOwnedParkingLot(userId, parkingLotId)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }

    // TODO getActiveParkingLot 테스트
    // TODO validateExistence 테스트
}
