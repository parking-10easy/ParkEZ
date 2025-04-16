package com.parkez.parkinglot.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotReaderTest {

    @InjectMocks
    private ParkingLotReader parkingLotReader;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    private final PageRequest pageRequest = new PageRequest(1, 10);
    Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.getPage() - 1, pageRequest.getSize());

    private User getOwnerUser2() {
        User owner2 = User.builder()
                .email("owner2@test.com")
                .nickname("테스트 소유자2")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner2, "id", 2L);
        return owner2;
    }

    private ParkingLot getParkingLot() {
        User ownerUser = User.builder()
                .email("owner@example.com")
                .nickname("Owner")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(ownerUser, "id", 1L);

        return ParkingLot.builder()
                .owner(ownerUser)
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 131")
                .images(new ArrayList<>())
                .build();
    }

    private ParkingLotSearchResponse getParkingLotResponse1() {
        return ParkingLotSearchResponse.builder()
                .parkingLotId(1L)
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 131")
                .build();
    }

    private ParkingLotSearchResponse getParkingLotResponse2() {
        return ParkingLotSearchResponse.builder()
                .parkingLotId(2L)
                .name("어려운주차장")
                .address("서울시 강남구 테헤란로 501")
                .build();
    }

    private MyParkingLotSearchResponse getMyParkingLotResponse1() {
        return MyParkingLotSearchResponse.builder()
                .parkingLotId(1L)
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 131")
                .build();
    }

    private MyParkingLotSearchResponse getMyParkingLotResponse2() {
        return MyParkingLotSearchResponse.builder()
                .parkingLotId(2L)
                .name("어려운주차장")
                .address("서울시 강남구 테헤란로 501")
                .build();
    }

    @Nested
    class searchParkingLotsByConditions {
        @Test
        void 검색_조건이_없을_때_주차장을_전체_조회한다() {
            // given
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();
            ParkingLotSearchResponse parkingLot2 = getParkingLotResponse2();
            List<ParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1, parkingLot2);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            when(parkingLotRepository.searchParkingLotsByConditions(
                    null, null, null, null, null, pageable
            )).thenReturn(page);

            when(parkingLotRepository.findImageListByParkingLotId(1L))
                    .thenReturn(List.of("image1.jpg", "image2.jpg"));

            when(parkingLotRepository.findImageListByParkingLotId(2L))
                    .thenReturn(List.of("image3.jpg"));

            // when
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(
                    null, null, null, null, null,
                    pageRequest.getPage(), pageRequest.getSize()
            );

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address", "images")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress(), parkingLot1.getImages()),
                            tuple(parkingLot2.getName(), parkingLot2.getAddress(), parkingLot2.getImages())
                    );
        }

        @Test
        void 이름으로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();
            List<ParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            String name = parkingLot1.getName();

            when(parkingLotRepository.searchParkingLotsByConditions(
                    name, null, null, null, null, pageable
            )).thenReturn(page);

            when(parkingLotRepository.findImageListByParkingLotId(1L))
                    .thenReturn(List.of("image1.jpg", "image2.jpg"));

            // when
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(
                    name, null, null, null, null,
                    pageRequest.getPage(), pageRequest.getSize()
            );

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address", "images")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress(), parkingLot1.getImages())
                    );
        }

        @Test
        void 주소로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();
            List<ParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            String address = parkingLot1.getAddress();

            when(parkingLotRepository.searchParkingLotsByConditions(
                    null, address, null, null, null, pageable
            )).thenReturn(page);

            when(parkingLotRepository.findImageListByParkingLotId(1L))
                    .thenReturn(List.of("image1.jpg", "image2.jpg"));

            // when
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(
                    null, address, null, null, null,
                    pageRequest.getPage(), pageRequest.getSize()
            );

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address", "images")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress(), parkingLot1.getImages())
                    );


        }

        @Test
        void 이름과_주소로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();
            List<ParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            String name = parkingLot1.getName();
            String address = parkingLot1.getAddress();

            when(parkingLotRepository.searchParkingLotsByConditions(
                    name, address, null, null, null, pageable
            )).thenReturn(page);

            when(parkingLotRepository.findImageListByParkingLotId(1L))
                    .thenReturn(List.of("image1.jpg", "image2.jpg"));

            // when
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(
                    name, address, null, null, null,
                    pageRequest.getPage(), pageRequest.getSize()
            );

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address", "images")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress(), parkingLot1.getImages())
                    );

        }

        @Test
        void 이름_주소_없이_사용자_위치로_인근_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();
            List<ParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());

            Double userLatitude = 37.500066200;
            Double userLongitude = 127.032926912;
            Integer radiusInMeters = 10000;

            when(parkingLotRepository.searchParkingLotsByConditions(
                    null, null, userLatitude, userLongitude, radiusInMeters, pageable
            )).thenReturn(page);

            when(parkingLotRepository.findImageListByParkingLotId(parkingLot1.getParkingLotId()))
                    .thenReturn(List.of("img1.jpg", "img2.jpg"));

            // when
            Page<ParkingLotSearchResponse> result = parkingLotReader.searchParkingLotsByConditions(
                    null, null, userLatitude, userLongitude, radiusInMeters,
                    pageRequest.getPage(), pageRequest.getSize()
            );

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address", "images")
                    .containsExactly(
                            tuple(parkingLot1.getName(), parkingLot1.getAddress(), parkingLot1.getImages())
                    );

        }
    }

    @Nested
    class searchParkingLotById {
        @Test
        void 아이디로_주차장을_단건_조회한다() {
            // given
            Long parkingLotId = 1L;
            ParkingLotSearchResponse parkingLot1 = getParkingLotResponse1();

            when(parkingLotRepository.searchParkingLotById(parkingLotId)).thenReturn(parkingLot1);

            when(parkingLotRepository.findImageListByParkingLotId(parkingLot1.getParkingLotId()))
                    .thenReturn(List.of("img1.jpg", "img2.jpg"));

            // when
            ParkingLotSearchResponse result = parkingLotReader.searchParkingLotById(parkingLotId);

            // then
            assertNotNull(result);
            assertEquals("참쉬운주차장", result.getName());
            assertThat(result.getImages()).containsExactly("img1.jpg", "img2.jpg");
        }

        @Test
        void 유효하지_않은_아이디로_주차장_단건_조회에_실패한다() {
            // given
            Long parkingLotId = -1L;
            when(parkingLotRepository.searchParkingLotById(parkingLotId)).thenReturn(null);

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
            MyParkingLotSearchResponse parkingLot1 = getMyParkingLotResponse1();
            MyParkingLotSearchResponse parkingLot2 = getMyParkingLotResponse2();
            List<MyParkingLotSearchResponse> parkingLotList = Arrays.asList(parkingLot1, parkingLot2);
            Page<MyParkingLotSearchResponse> page = new PageImpl<>(parkingLotList, pageable, parkingLotList.size());
            Long userId = 1L;
            when(parkingLotRepository.findMyParkingLots(userId, pageable)).thenReturn(page);

            // when
            Page<MyParkingLotSearchResponse> result = parkingLotReader.getMyParkingLots(userId, pageRequest.getPage(), pageRequest.getSize());

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
            Long nonParkingLotOwnerId = -1L;
            Page<MyParkingLotSearchResponse> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
            when(parkingLotRepository.findMyParkingLots(nonParkingLotOwnerId, pageable)).thenReturn(emptyPage);

            // when
            Page<MyParkingLotSearchResponse> result = parkingLotReader.getMyParkingLots(nonParkingLotOwnerId, pageRequest.getPage(), pageRequest.getSize());

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
            ParkingLot parkingLot1 = getParkingLot();
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

        @Test
        void 특정_주차장의_소유자_본인이_아니면_NOT_PARKING_LOT_OWNER_예외가_발생한다() {
            // given
            User nonOwner = getOwnerUser2();
            ParkingLot parkingLot = mock(ParkingLot.class);

            when(parkingLotRepository.findByIdAndDeletedAtIsNull(anyLong()))
                    .thenReturn(Optional.of(parkingLot));
            when(parkingLot.isOwned(anyLong())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingLotReader.getOwnedParkingLot(nonOwner.getId(), parkingLot.getId()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER.getDefaultMessage());
        }
    }

    @Nested
    class ValidateExistence {
        @Test
        void 특정_주차장이_존재하면_예외가_발생하지_않는다() {
            // given
            Long validParkingLotId = 1L;
            when(parkingLotRepository.existsByIdAndDeletedAtIsNull(anyLong())).thenReturn(true);

            // when & then
            assertThatCode(() -> parkingLotReader.validateExistence(validParkingLotId))
                    .doesNotThrowAnyException();
        }

        @Test
        void 특정_주차장이_존재하지_않으면_NOT_FOUND_예외가_발생한다() {
            // given
            Long invalidParkingLotId = -1L;
            when(parkingLotRepository.existsByIdAndDeletedAtIsNull(anyLong())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingLotReader.validateExistence(invalidParkingLotId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }
    }
}
