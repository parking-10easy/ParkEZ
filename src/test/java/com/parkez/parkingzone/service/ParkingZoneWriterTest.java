package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneWriterTest {

    @Mock
    private ParkingZoneReader parkingZoneReader;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private ParkingLot parkingLot;

    @InjectMocks
    private ParkingZoneWriter parkingZoneWriter;

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

    private User getOwner() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("테스트 소유자")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;
    }

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(getOwner())
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

    private ParkingZoneUpdateRequest getUpdateRequest() {
        return ParkingZoneUpdateRequest.builder()
                .name("A구역 수정")
                .build();
    }

    private ParkingZone getUpdateParkingZone() {
        ParkingLot parkingLot = getParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역 수정")
                .imageUrl("http://example.com/image.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
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

    @Nested
    class CreateParkingZone {
        @Test
        void ParkingZone을_생성할_수_있다() {
            // given
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            when(parkingZoneRepository.save(any(ParkingZone.class))).thenReturn(parkingZone);

            // when
            ParkingZone result = parkingZoneWriter.createParkingZone(createRequest.getName(),createRequest.getImageUrl(), parkingLot);

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }
    }

    @Nested
    class UpdateParkingZone {
        @Test
        void ParkingZone을_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateRequest updateRequest = getUpdateRequest();

            when(parkingZoneReader.getParkingZone(1L)).thenReturn(parkingZone);

            // when
            parkingZoneWriter.updateParkingZone(authUser,1L, updateRequest.getName());

            // then
            assertThat(parkingZone.getName()).isEqualTo(updateRequest.getName());
            Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
        }

        @Test
        void 존재하지_않는_주차공간을_수정하면_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateRequest updateRequest = getUpdateRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZone(authUser, parkingZone.getId(), updateRequest.getName()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_수정_소유자_본인이_아니면_FORBIDDEN_TO_UPDATE_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateRequest updateRequest = getUpdateRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willReturn(parkingZone);

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZone(nonOwner, parkingZone.getId(), updateRequest.getName()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE.getDefaultMessage());
        }

        @Test
        void ParkingZone의_상태를_변경할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            when(parkingZoneReader.getParkingZone(1L)).thenReturn(parkingZone);

            // when
            parkingZoneWriter.updateParkingZoneStatus(authUser,1L, updateStatusRequest.getStatus());

            // then
            assertThat(parkingZone.getStatus()).isEqualTo(updateStatusRequest.getStatus());
            Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
        }

        @Test
        void 존재하지_않는_주차공간의_상태를_변경하면_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZoneStatus(authUser, parkingZone.getId(), updateStatusRequest.getStatus()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_상태변경_소유자_본인이_아니면_FORBIDDEN_TO_UPDATE_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateStatusRequest updateStatusRequest = getUpdateStatusRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willReturn(parkingZone);

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZoneStatus(nonOwner, parkingZone.getId(), updateStatusRequest.getStatus()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE.getDefaultMessage());
        }

        @Test
        void ParkingZone의_이미지를_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            when(parkingZoneReader.getParkingZone(1L)).thenReturn(parkingZone);

            // when
            parkingZoneWriter.updateParkingZoneImage(authUser,1L, updateImageRequest.getImageUrl());

            // then
            assertThat(parkingZone.getImageUrl()).isEqualTo(updateImageRequest.getImageUrl());
            Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
        }

        @Test
        void 존재하지_않는_주차공간의_이미지를_수정하면_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZoneImage(authUser, parkingZone.getId(), updateImageRequest.getImageUrl()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_이미지수정_소유자_본인이_아니면_FORBIDDEN_TO_UPDATE_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneUpdateImageRequest updateImageRequest = getUpdateImageRequest();

            given(parkingZoneReader.getParkingZone(anyLong())).willReturn(parkingZone);

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.updateParkingZoneImage(nonOwner, parkingZone.getId(), updateImageRequest.getImageUrl()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE.getDefaultMessage());
        }
    }

    @Nested
    class DeleteParkingZone {
        @Test
        void ParkingZone을_삭제할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            ParkingZone parkingZone = getParkingZone();

            when(parkingZoneReader.getParkingZone(anyLong())).thenReturn(parkingZone);
            doAnswer(invocation -> {
                ReflectionTestUtils.setField(parkingZone, "deletedAt", LocalDateTime.now());
                return null;
            }).when(parkingZoneRepository).softDeleteById(eq(1L), any(LocalDateTime.class));

            // when
            parkingZoneWriter.deleteParkingZone(authUser, 1L,  LocalDateTime.now());

            // then
            verify(parkingZoneRepository).softDeleteById(eq(1L), any(LocalDateTime.class));
            assertThat(ReflectionTestUtils.getField(parkingZone, "deletedAt")).isNotNull();
        }

        @Test
        void 존재하지_않는_주차공간을_삭제하면_예외가_발생한다() {
            // given
            ParkingZone parkingZone = getParkingZone();
            AuthUser authUser = getAuthUser();

            given(parkingZoneReader.getParkingZone(anyLong())).willThrow(
                    new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.deleteParkingZone(authUser, parkingZone.getId(),LocalDateTime.now()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 주차공간_삭제_소유자_본인이_아니면_FORBIDDEN_TO_DELETE_예외가_발생한다() {
            // given
            AuthUser nonOwner = getAuthUser2();
            ParkingZone parkingZone = getParkingZone();

            given(parkingZoneReader.getParkingZone(anyLong())).willReturn(parkingZone);

            // when & then
            assertThatThrownBy(() -> parkingZoneWriter.deleteParkingZone(nonOwner, parkingZone.getId(), LocalDateTime.now()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.FORBIDDEN_TO_DELETE.getDefaultMessage());
        }

    }





}