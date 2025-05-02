package com.parkez.parkingzone.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateNameRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneServiceTest {

    @Mock
    private ParkingZoneWriter parkingZoneWriter;

    @Mock
    private ParkingZoneReader parkingZoneReader;

    @Mock
    private ParkingLotReader parkingLotReader;

    @Mock
    private ReservationReader reservationReader;

    @InjectMocks
    private ParkingZoneService parkingZoneService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(parkingZoneService, "defaultImageUrl", "http://example.com/image.jpg");
    }

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .name("Main Parking Lot")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    private ParkingZone getParkingZone() {
        ParkingLot parkingLot = getParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
    }

    private ParkingZoneCreateRequest getCreateRequest() {
        return ParkingZoneCreateRequest.builder()
                .parkingLotId(1L)
                .name("A구역")
                .build();
    }

    private Page<ParkingZoneResponse> getParkingZoneResponses() {
        List<ParkingZoneResponse> list = List.of(
                new ParkingZoneResponse(1L,1L, "A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE),
                new ParkingZoneResponse(2L,1L, "B구역","http://example.com/image2.jpg", ParkingZoneStatus.AVAILABLE)
        );
        return new PageImpl<>(list);
    }

    private ParkingZoneUpdateNameRequest getUpdateNameRequest() {
        return ParkingZoneUpdateNameRequest.builder()
                .name("A구역 수정")
                .build();
    }

    private ParkingZoneUpdateStatusRequest getUpdateStatusRequest() {
        return ParkingZoneUpdateStatusRequest.builder()
                .status("UNAVAILABLE")
                .build();
    }
    private ParkingZoneUpdateImageRequest getUpdateImageRequest() {
        return ParkingZoneUpdateImageRequest.builder()
                .imageUrl("http://example.com/image수정.jpg")
                .build();
    }

    private AuthUser getAuthUser() {
        return AuthUser.builder()
                .id(1L)
                .email("owner@test.com")
                .roleName(UserRole.ROLE_OWNER.name())
                .nickname("테스트 소유자")
                .build();
    }

    private AuthUser getAuthUser2() {
        return AuthUser.builder()
                .id(2L)
                .email("owner2@test.com")
                .roleName(UserRole.ROLE_OWNER.name())
                .nickname("테스트 소유자2")
                .build();
    }

    @Nested
    class CreateParkingZone {
        @Test
        void 주차공간_생성_특정_주차장에_대한_주차공간을_정상적으로_생성할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingLot parkingLot = getParkingLot();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            when(parkingLotReader.getOwnedParkingLot(any(), anyLong())).thenReturn(parkingLot);
            when(parkingZoneWriter.createParkingZone(anyString(), anyString(), any(ParkingLot.class))).thenReturn(parkingZone);

            // when
            ParkingZoneResponse result = parkingZoneService.createParkingZone(authUser, createRequest);

            // then
            assertThat(result).extracting("id","parkingLotId","name","imageUrl","status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }

        @Test
        void 주차공간_생성_존재하지_않는_주차장으로_생성하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            given(parkingLotReader.getOwnedParkingLot(any(),anyLong())).willThrow(
                    new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneService.createParkingZone(authUser, createRequest))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_생성_공공데이터로_생성된_주차장의_주차공간_생성시_PUBLIC_DATA_CREATION_NOT_ALLOWED_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingLot parkingLot = getParkingLot();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            ReflectionTestUtils.setField(parkingLot, "sourceType", SourceType.PUBLIC_DATA);
            given(parkingLotReader.getOwnedParkingLot(authUser.getId(), createRequest.getParkingLotId()))
                    .willReturn(parkingLot);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.createParkingZone(authUser, createRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PUBLIC_DATA_CREATION_NOT_ALLOWED.getDefaultMessage());
        }
    }

    @Nested
    class GetParkingZones {
        @Test
        void 주차공간_다건조회_특정_주차장에_대한_주차공간을_정상적으로_다건조회할_수_있다() {
            // given
            Long parkingLotId = 1L;
            Page<ParkingZoneResponse> parkingZoneResponses = getParkingZoneResponses();

            doNothing().when(parkingLotReader).validateExistence(parkingLotId);
            when(parkingZoneReader.getParkingZones(anyInt(), anyInt(), eq(parkingLotId))).thenReturn(parkingZoneResponses);

            // when
            PageRequest pageRequest = new PageRequest(1,10);
            Page<ParkingZoneResponse> result = parkingZoneService.getParkingZones(pageRequest, parkingLotId);

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .contains(tuple(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE),
                            tuple(2L,1L, "B구역","http://example.com/image2.jpg", ParkingZoneStatus.AVAILABLE));
        }

        @Test
        void 주차공간_다건조회_존재하지_않는_주차장_아이디로_조회하면_NOT_FOUND_예외가_발생한다() {
            // given
            Long INVALID_PARKING_LOT_ID = -1L;
            doThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND))
                    .when(parkingLotReader).validateExistence(anyLong());

            // when & then
            PageRequest pageRequest = new PageRequest(1,10);
            assertThatThrownBy(() -> parkingZoneService.getParkingZones(pageRequest,INVALID_PARKING_LOT_ID))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }
    }

    @Nested
    class GetParkingZone {
        @Test
        void 주차공간_단건조회_특정_주차공간을_정상적으로_단건조회할_수_있다() {
            // given
            ParkingZone parkingZone = getParkingZone();
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when
            ParkingZoneResponse result = parkingZoneService.getParkingZone(parkingZone.getId());

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }
    }

    @Nested
    class UpdateParkingZone {
        @Test
        void 주차공간_이름수정_특정_주차공간의_이름을_정상적으로_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateNameRequest updateRequest = getUpdateNameRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(),anyLong())).willReturn(true);

            // when
            parkingZoneService.updateParkingZoneName(authUser, parkingZone.getId(), updateRequest);

            // then
            assertThat(parkingZone.getName()).isEqualTo(updateRequest.getName());
        }

        @Test
        void 주차공간_이름수정_존재하지_않는_주차공간을_수정하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateNameRequest updateNameRequest = getUpdateNameRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneName(authUser, parkingZone.getId(), updateNameRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_이름수정_소유자_본인이_아니면_FORBIDDEN_TO_ACTION_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateNameRequest updateNameRequest = getUpdateNameRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(), anyLong())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneName(nonOwner, parkingZone.getId(), updateNameRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_ACTION.getDefaultMessage());
        }

        @Test
        void 주차공간_상태수정_특정_주차공간의_상태를_정상적으로_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(),anyLong())).willReturn(true);
            given(reservationReader.existsActiveReservationByParkingZoneId(anyLong())).willReturn(false);

            // when
            parkingZoneService.updateParkingZoneStatus(authUser, parkingZone.getId(), updateStatusRequest);

            // then
            assertThat(parkingZone.getStatus().name()).isEqualTo(updateStatusRequest.getStatus());
        }

        @Test
        void 주차공간_상태수정_존재하지_않는_주차공간을_수정하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneStatus(authUser, parkingZone.getId(), updateStatusRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_상태수정_소유자_본인이_아니면_FORBIDDEN_TO_ACTION_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(), anyLong())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneStatus(nonOwner, parkingZone.getId(), updateStatusRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_ACTION.getDefaultMessage());
        }

        @Test
        void 주차공간_상태수정_UNAVAILABLE_변경시_기존_예약이_존재할_경우_RESERVED_ZONE_STATUS_CHANGE_FORBIDDEN_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(), anyLong())).willReturn(true);
            given(reservationReader.existsActiveReservationByParkingZoneId(anyLong())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneStatus(authUser, parkingZone.getId(), updateStatusRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.RESERVED_ZONE_STATUS_CHANGE_FORBIDDEN.getDefaultMessage());
        }

        @Test
        void 주차공간_이미지수정_특정_주차공간의_이미지를_정상적으로_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(),anyLong())).willReturn(true);

            // when
            parkingZoneService.updateParkingZoneImage(authUser, parkingZone.getId(), updateImageRequest);

            // then
            assertThat(parkingZone.getImageUrl()).isEqualTo(updateImageRequest.getImageUrl());
        }

        @Test
        void 주차공간_이미지수정_존재하지_않는_주차공간을_수정하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneImage(authUser, parkingZone.getId(), updateImageRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_이미지수정_소유자_본인이_아니면_FORBIDDEN_TO_ACTION_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(), anyLong())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.updateParkingZoneImage(nonOwner, parkingZone.getId(), updateImageRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_ACTION.getDefaultMessage());
        }
    }

    @Nested
    class DeleteParkingZone {
        @Test
        void 주차공간_삭제_특정_주차공간을_정상적으로_삭제할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            LocalDateTime deletedAt = LocalDateTime.now();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(),anyLong())).willReturn(true);

            // when
            parkingZoneService.deleteParkingZone(authUser, parkingZone.getId(),deletedAt);

            // then
            verify(parkingZoneWriter, times(1)).deleteParkingZone(parkingZone.getId(),deletedAt);
        }

        @Test
        void 주차공간_삭제_존재하지_않는_주차공간을_수정하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneService.deleteParkingZone(authUser, parkingZone.getId(), LocalDateTime.now()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_삭제_소유자_본인이_아니면_FORBIDDEN_TO_ACTION_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(), anyLong())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.deleteParkingZone(nonOwner, parkingZone.getId(), LocalDateTime.now()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_ACTION.getDefaultMessage());
        }

        @Test
        void 주차공간_삭제_기존_예약이_존재할_경우_RESERVED_ZONE_DELETE_FORBIDDEN_예외가_발생한다() {

            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(parkingZoneReader.isOwnedParkingZone(anyLong(),anyLong())).willReturn(true);
            given(reservationReader.existsActiveReservationByParkingZoneId(anyLong())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> parkingZoneService.deleteParkingZone(authUser, parkingZone.getId(), LocalDateTime.now()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.RESERVED_ZONE_DELETE_FORBIDDEN.getDefaultMessage());
        }
    }
}