package com.parkez.parkingzone.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.user.domain.enums.UserRole;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private ParkingZoneService parkingZoneService;

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
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    private Page<ParkingZoneResponse> getParkingZoneResponses() {
        List<ParkingZoneResponse> list = List.of(
                new ParkingZoneResponse(1L,1L, "A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE),
                new ParkingZoneResponse(2L,1L, "B구역","http://example.com/image2.jpg", ParkingZoneStatus.AVAILABLE)
        );
        return new PageImpl<>(list);
    }

    private ParkingZoneUpdateRequest getUpdateRequest() {
        return ParkingZoneUpdateRequest.builder()
                .name("A구역 수정")
                .build();
    }

    private ParkingZoneUpdateStatusRequest getUpdateStatusRequest() {
        return ParkingZoneUpdateStatusRequest.builder()
                .status(ParkingZoneStatus.UNAVAILABLE)
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

    @Nested
    class CreateParkingZone {
        @Test
        void 주차_공간_생성_정상적으로_생성할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingLot parkingLot = getParkingLot();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            when(parkingLotReader.getOwnedParkingLot(any(), anyLong())).thenReturn(parkingLot);
            when(parkingZoneWriter.createParkingZone(parkingZone.getName(),parkingZone.getImageUrl(), parkingLot)).thenReturn(parkingZone);

            // when
            ParkingZoneCreateResponse result = parkingZoneService.createParkingZone(authUser, createRequest);

            // then
            assertThat(result).extracting("id","parkingLotId","name","imageUrl","status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }

        @Test
        void 주차_공간_생성_존재하지_않는_주차장으로_생성하면_NOT_FOUND_예외가_발생한다() {
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
    }

    @Nested
    class GetParkingZones {
        @Test
        void 주차_공간_다건조회_정상적으로_다건조회할_수_있다() {
            // given
            Long parkingLotId = 1L;
            Page<ParkingZoneResponse> parkingZoneResponses = getParkingZoneResponses();

            doNothing().when(parkingLotReader).validateExistence(parkingLotId);
            when(parkingZoneReader.getParkingZones(anyInt(), anyInt(), eq(parkingLotId))).thenReturn(parkingZoneResponses);

            // when
            PageRequest pageRequest = new PageRequest(1,10);
            Page<ParkingZoneResponse> result = parkingZoneService.getParkingZones(pageRequest, parkingLotId);

            // then
            assertThat(result.getContent().get(0)).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
            assertThat(result.getContent().get(1)).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(2L,1L, "B구역","http://example.com/image2.jpg", ParkingZoneStatus.AVAILABLE);
        }

        @Test
        void 주차_공간_다건조회_존재하지_않는_주차장_아이디로_조회하면_NOT_FOUND_예외가_발생한다() {
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
        void 주차_공간_단건조회_정상적으로_단건조회할_수_있다() {
            // given
            ParkingZone parkingZone = getParkingZone();
            given(parkingZoneReader.getParkingZone(anyLong())).willReturn(parkingZone);

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
        void 주차_공간_수정_정상적으로_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateRequest updateRequest = getUpdateRequest();

            // when
            parkingZoneService.updateParkingZone(authUser, parkingZone.getId(), updateRequest);

            // then
            verify(parkingZoneWriter, times(1)).updateParkingZone(authUser,1L, updateRequest.getName());
        }

        @Test
        void 주차_공간_상태수정_정상적으로_상태를_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            // when
            parkingZoneService.updateParkingZoneStatus(authUser, parkingZone.getId(), updateStatusRequest);

            // then
            verify(parkingZoneWriter, times(1)).updateParkingZoneStatus(authUser,1L, updateStatusRequest.getStatus());
        }

        @Test
        void 주차_공간_이미지수정_정상적으로_이미지를_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            // when
            parkingZoneService.updateParkingZoneImage(authUser, parkingZone.getId(), updateImageRequest);

            // then
            verify(parkingZoneWriter, times(1)).updateParkingZoneImage(authUser,1L, updateImageRequest.getImageUrl());
        }
    }

    @Nested
    class DeleteParkingZone {
        @Test
        void 주차_공간_삭제_정상적으로_주차공간을_삭제할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            LocalDateTime deletedAt = LocalDateTime.now();


            // when
            parkingZoneService.deleteParkingZone(authUser, parkingZone.getId(),deletedAt);

            // then
            verify(parkingZoneWriter, times(1)).deleteParkingZone(authUser,parkingZone.getId(),deletedAt);
        }
    }
}