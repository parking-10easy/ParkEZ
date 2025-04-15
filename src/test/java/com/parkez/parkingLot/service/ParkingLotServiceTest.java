package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import com.parkez.common.dto.request.PageRequest;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotServiceTest {

    @InjectMocks
    private ParkingLotService parkingLotService;

    @Mock
    private ParkingLotWriter parkingLotWriter;

    @Mock
    private ParkingLotReader parkingLotReader;

    @Mock
    private UserReader userReader;

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

    private AuthUser getAuthUserOwner() {
        User user = getOwnerUser();
        return AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName("ROLE_OWNER")
                .build();
    }

    private AuthUser getAuthUserNotParkingLotOwner() {
        return AuthUser.builder()
                .id(2L)
                .email("authUserNotParkingLotOwner@example.com")
                .roleName("ROLE_OWNER")
                .build();
    }

    private ParkingLot getParkingLot() {
        ParkingLotRequest request = getParkingLotRequest();
        User ownerUser = getOwnerUser();
        return ParkingLot.builder()
                .owner(ownerUser)
                .name(request.getName())
                .address(request.getAddress())
                .images(new ArrayList<>())
                .build();
    }

    private ParkingLotRequest getParkingLotRequest() {
        return ParkingLotRequest.builder()
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 123")
                .build();
    }

    private ParkingLotResponse getParkingLotResponse() {
        ParkingLot parkingLot = getParkingLot();
        return ParkingLotResponse.from(parkingLot);
    }

    private ParkingLotSearchResponse getParkingLotSearchResponse1() {
        return ParkingLotSearchResponse.builder()
                .name("참쉬운주차장")
                .address("서울시 강남구 테헤란로 123")
                .build();
    }

    private ParkingLotSearchResponse getParkingLotSearchResponse2() {
        return ParkingLotSearchResponse.builder()
                .name("어려운주차장")
                .address("서울시 강남구 테헤란로 111")
                .build();
    }

    private ParkingLotRequest getUpdatedParkingLot() {
        return ParkingLotRequest.builder()
                .name("수정된 주차장")
                .address("수정된 주소")
                .build();
    }

    private ParkingLotStatusRequest getParkingLotStatusRequest() {
        return ParkingLotStatusRequest.builder()
                .status(String.valueOf(ParkingLotStatus.CLOSED))
                .build();
    }

    private ParkingLotImagesRequest getParkingLotImagesRequest() {
        return ParkingLotImagesRequest.builder()
                .imageUrls(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"))
                .build();
    }

    private MyParkingLotSearchResponse getMyParkingLotResponse1() {
        return MyParkingLotSearchResponse.builder()
                .name("배고픈 주차장")
                .address("경주시 황남동 포석로 111")
                .build();
    }

    private MyParkingLotSearchResponse getMyParkingLotResponse2() {
        return MyParkingLotSearchResponse.builder()
                .name("배부른 주차장")
                .address("경주시 황남동 포석로 222")
                .build();
    }


    @Nested
    class CreateParkingLot {

        @Test
        void 주차장을_생성한다() {
            // given
            AuthUser authUser = getAuthUserOwner();
            User owner = getOwnerUser();
            ParkingLot parkingLot = getParkingLot();
            when(userReader.getActiveUserById(authUser.getId())).thenReturn(owner);
            when(parkingLotWriter.createParkingLot(any(ParkingLot.class))).thenReturn(parkingLot);

            // when
            ParkingLotRequest parkingLotRequest = getParkingLotRequest();
            ParkingLotResponse response = parkingLotService.createParkingLot(authUser, parkingLotRequest);

            // then
            ParkingLotResponse expectedResponse = getParkingLotResponse();
            assertNotNull(response);
            assertEquals(expectedResponse.getName(), response.getName());

            // 단순 호출 검증
            verify(parkingLotWriter).createParkingLot(any(ParkingLot.class));
        }
    }

    @Nested
    class searchParkingLotsByConditions {

        @Test
        void 검색_조건이_없을_때_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse searchResponse1 = getParkingLotSearchResponse1();
            ParkingLotSearchResponse searchResponse2 = getParkingLotSearchResponse2();

            List<ParkingLotSearchResponse> responses = Arrays.asList(searchResponse1, searchResponse2);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());

            ParkingLotSearchRequest searchRequest = ParkingLotSearchRequest.builder()
                    .name(null)
                    .address(null)
                    .build();
            when(parkingLotReader.searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            Page<ParkingLotSearchResponse> result = parkingLotService.searchParkingLotsByConditions(searchRequest, pageRequest);

            // then
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(searchResponse1.getName(), searchResponse1.getAddress()),
                            tuple(searchResponse2.getName(), searchResponse2.getAddress())
                    );
            verify(parkingLotReader).searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize());
        }

        @Test
        void 이름으로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse searchResponse1 = getParkingLotSearchResponse1();
            List<ParkingLotSearchResponse> responses = Arrays.asList(searchResponse1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());
            ParkingLotSearchRequest searchRequest = ParkingLotSearchRequest.builder()
                    .name(searchResponse1.getName())
                    .address(null)
                    .build();
            when(parkingLotReader.searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            Page<ParkingLotSearchResponse> result = parkingLotService.searchParkingLotsByConditions(searchRequest, pageRequest);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(searchResponse1.getName(), searchResponse1.getAddress())
                    );
            verify(parkingLotReader).searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize());
        }

        @Test
        void 주소로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse searchResponse1 = getParkingLotSearchResponse1();
            List<ParkingLotSearchResponse> responses = Arrays.asList(searchResponse1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());
            ParkingLotSearchRequest searchRequest = ParkingLotSearchRequest.builder()
                    .name(null)
                    .address(searchResponse1.getAddress())
                    .build();
            when(parkingLotReader.searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            Page<ParkingLotSearchResponse> result = parkingLotService.searchParkingLotsByConditions(searchRequest, pageRequest);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(searchResponse1.getName(), searchResponse1.getAddress())
                    );
            verify(parkingLotReader).searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize());
        }

        @Test
        void 이름과_주소로_주차장을_조회한다() {
            // given
            ParkingLotSearchResponse searchResponse1 = getParkingLotSearchResponse1();
            List<ParkingLotSearchResponse> responses = Arrays.asList(searchResponse1);
            Page<ParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());
            ParkingLotSearchRequest searchRequest = ParkingLotSearchRequest.builder()
                    .name(searchResponse1.getName())
                    .address(searchResponse1.getAddress())
                    .build();
            when(parkingLotReader.searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            Page<ParkingLotSearchResponse> result = parkingLotService.searchParkingLotsByConditions(searchRequest, pageRequest);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(searchResponse1.getName(), searchResponse1.getAddress())
                    );
            verify(parkingLotReader).searchParkingLotsByConditions(searchRequest.getName(), searchRequest.getAddress(), pageRequest.getPage(), pageRequest.getSize());
        }
    }

    @Nested
    class searchParkingLotById {

        @Test
        void 아이디로_주차장을_단건_조회한다() {
            // given
            Long parkingLotId = 1L;
            ParkingLot parkingLot = getParkingLot();
            when(parkingLotReader.searchParkingLotById(parkingLotId)).thenReturn(parkingLot);

            // when
            ParkingLotSearchResponse result = parkingLotService.searchParkingLotById(parkingLotId);

            // then
            assertNotNull(result);
            assertEquals(parkingLot.getName(), result.getName());
            verify(parkingLotReader).searchParkingLotById(parkingLotId);
        }
    }

    @Nested
    class getMyParkingLots {
        @Test
        void 본인이_소유한_주차장을_조회한다() {
            // given
            Long userId = getAuthUserOwner().getId();

            MyParkingLotSearchResponse MyParkingResponse1 = getMyParkingLotResponse1();
            MyParkingLotSearchResponse MyParkingResponse2 = getMyParkingLotResponse2();

            List<MyParkingLotSearchResponse> responses = Arrays.asList(MyParkingResponse1, MyParkingResponse2);
            Page<MyParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());

            when(parkingLotReader.getMyParkingLots(userId, pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            AuthUser authUser = getAuthUserOwner();
            Page<MyParkingLotSearchResponse> result = parkingLotService.getMyParkingLots(authUser, pageRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting("name", "address")
                    .containsExactly(
                            tuple(MyParkingResponse1.getName(), MyParkingResponse1.getAddress()),
                            tuple(MyParkingResponse2.getName(), MyParkingResponse2.getAddress())
                    );
        }

        @Test
        void 본인_소유가_아닌_주차장은_조회가_되지_않는다() {
            // given
            Long userIdNotParkingOwner = getAuthUserNotParkingLotOwner().getId();
            Page<MyParkingLotSearchResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(parkingLotReader.getMyParkingLots(userIdNotParkingOwner, pageRequest.getPage(), pageRequest.getSize())).thenReturn(page);

            // when
            AuthUser authUserNotOwner = getAuthUserNotParkingLotOwner();
            Page<MyParkingLotSearchResponse> result = parkingLotService.getMyParkingLots(authUserNotOwner, pageRequest);

            assertThat(result).isNotNull();
            assertEquals(0, result.getTotalElements());
        }
    }


    @Nested
    class updateParkingLot {
        @Test
        void 특정_주차장을_수정한다() {
            //given
            Long parkingLotId = 1L;
            Long userId = getAuthUserOwner().getId();
            ParkingLot parkingLot = getParkingLot();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId)).thenReturn(parkingLot);

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotRequest request = getParkingLotRequest();
            parkingLotService.updateParkingLot(authUser, parkingLotId, request);

            //then
            assertThat(parkingLot)
                    .extracting("name", "address")
                    .containsExactly(
                            request.getName(), request.getAddress()
                    );
        }

        @Test
        void 소유자가_아니면_특정_주차장_수정에_실패한다() {
            // given
            Long parkingLotId = 1L;
            Long userIdNotParkingOwner = getAuthUserNotParkingLotOwner().getId();
            AuthUser authUserNotParkingOwner = getAuthUserNotParkingLotOwner();
            when(parkingLotReader.getOwnedParkingLot(userIdNotParkingOwner, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER));

            // when
            ParkingLotRequest request = getParkingLotRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLot(authUserNotParkingOwner, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_아이디로_주차장_수정에_실패한다() {
            // given
            Long parkingLotId = -1L;
            Long userId = getAuthUserOwner().getId();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotRequest request = getParkingLotRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLot(authUser, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class updateParkingLotStatus {
        @Test
        void 특정_주차장의_상태를_변경한다() {
            // given
            ParkingLot parkingLot = getParkingLot();
            parkingLot.updateStatus(ParkingLotStatus.CLOSED);
            Long userId = getAuthUserOwner().getId();
            Long parkingLotId = 1L;
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId)).thenReturn(parkingLot);

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotStatusRequest request = getParkingLotStatusRequest();
            parkingLotService.updateParkingLotStatus(authUser, parkingLotId, request);

            // then
            assertEquals(request.getStatus(), parkingLot.getStatus().name());
        }

        @Test
        void 소유자가_아니면_주차장_상태_변경에_실패한다() {
            // given
            Long parkingLotId = 1L;
            Long userIdNotParkingOwner = getAuthUserNotParkingLotOwner().getId();
            AuthUser authUserNotParkingOwner = getAuthUserNotParkingLotOwner();
            when(parkingLotReader.getOwnedParkingLot(userIdNotParkingOwner, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER));

            // when
            ParkingLotStatusRequest request = getParkingLotStatusRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLotStatus(authUserNotParkingOwner, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_아이디로_주차장_상태_변경에_실패한다() {
            // given
            Long parkingLotId = -1L;
            Long userId = getAuthUserOwner().getId();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotStatusRequest request = getParkingLotStatusRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLotStatus(authUser, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 잘못된_상태_값이_입력되면_예외를_발생한다() {
            // given
            ParkingLotStatusRequest invalidStatusRequest = ParkingLotStatusRequest.builder()
                    .status("invalidStatus")
                    .build();

            // when
            AuthUser authUser = getAuthUserOwner();
            Long parkingLotId = 1L;
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLotStatus(authUser, parkingLotId, invalidStatusRequest)
            );

            // then
            assertEquals(ParkingLotErrorCode.INVALID_PARKING_LOT_STATUS, exception.getErrorCode());
        }

    }

    @Nested
    class updateParkingLotImages {
        @Test
        void 특정_주차장_이미지를_수정한다() {
            // given
            Long parkingLotId = 1L;
            Long userId = getAuthUserOwner().getId();
            ParkingLot parkingLot = getParkingLot();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId)).thenReturn(parkingLot);

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotImagesRequest request = getParkingLotImagesRequest();
            parkingLotService.updateParkingLotImages(authUser, parkingLotId, request);

            //then
            List<ParkingLotImage> images = parkingLot.getImages();
            assertThat(images)
                    .extracting("imageUrl")
                    .containsExactly(request.getImageUrls().get(0), request.getImageUrls().get(1));
        }

        @Test
        void 소유자가_아니면_주차장_이미지_수정을_실패한다() {
            // given
            Long parkingLotId = 1L;
            Long userIdNotParkingOwner = getAuthUserNotParkingLotOwner().getId();
            AuthUser authUserNotParkingOwner = getAuthUserNotParkingLotOwner();
            when(parkingLotReader.getOwnedParkingLot(userIdNotParkingOwner, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER));

            // when
            ParkingLotImagesRequest request = getParkingLotImagesRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLotImages(authUserNotParkingOwner, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_아이디로_주차장_이미지_수정에_실패한다() {
            // given
            Long parkingLotId = -1L;
            Long userId = getAuthUserOwner().getId();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingLotImagesRequest request = getParkingLotImagesRequest();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.updateParkingLotImages(authUser, parkingLotId, request)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }


    @Nested
    class deleteParkingLot {
        @Test
        void 특정_주차장을_삭제한다() {
            // given
            Long parkingLotId = 1L;
            Long userId = getAuthUserOwner().getId();
            ParkingLot parkingLot = getParkingLot();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId)).thenReturn(parkingLot);

            // when
            AuthUser authUser = getAuthUserOwner();
            parkingLotService.deleteParkingLot(authUser, parkingLotId);

            // then
            verify(parkingLotWriter).deleteParkingLot(parkingLot);
        }

        @Test
        void 소유자가_아니면_주차장_삭제를_실패한다() {
            // given
            Long parkingLotId = 1L;
            Long userIdNotParkingOwner = getAuthUserNotParkingLotOwner().getId();
            AuthUser authUserNotParkingOwner = getAuthUserNotParkingLotOwner();
            when(parkingLotReader.getOwnedParkingLot(userIdNotParkingOwner, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER));

            // when
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.deleteParkingLot(authUserNotParkingOwner, parkingLotId)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_아이디로_주차장_삭제에_실패한다() {
            // given
            Long parkingLotId = -1L;
            Long userId = getAuthUserOwner().getId();
            when(parkingLotReader.getOwnedParkingLot(userId, parkingLotId))
                    .thenThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));

            // when
            AuthUser authUser = getAuthUserOwner();
            ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                    parkingLotService.deleteParkingLot(authUser, parkingLotId)
            );

            // then
            assertEquals(ParkingLotErrorCode.NOT_FOUND, exception.getErrorCode());
        }
    }
}
