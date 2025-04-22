package com.parkez.parkingzone.service;

import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneWriterTest {

    @Mock
    private ParkingZoneReader parkingZoneReader;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private ParkingLot parkingLot;

    @Mock
    private ParkingLotReader parkingLotReader;

    @InjectMocks
    private ParkingZoneWriter parkingZoneWriter;

    private String defaultImageUrl = "http://example.com/image.jpg";

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
                .imageUrl(defaultImageUrl)
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

    @Nested
    class CreateParkingZone {
        @Test
        void 주차공간_생성_특정_주차장에_대한_주차공간을_정상적으로_생성할_수_있다() {
            // given
            ParkingZone parkingZone = getParkingZone();
            ParkingZoneCreateRequest createRequest = getCreateRequest();

            when(parkingZoneRepository.save(any(ParkingZone.class))).thenReturn(parkingZone);

            // when
            ParkingZone result = parkingZoneWriter.createParkingZone(createRequest.getName(),defaultImageUrl, parkingLot);

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }
    }

    @Nested
    class DeleteParkingZone {
        @Test
        void 주차공간_삭제_특정_주차공간을_정상적으로_삭제할_수_있다() {
            // given
            ParkingZone parkingZone = getParkingZone();

            doAnswer(invocation -> {
                ReflectionTestUtils.setField(parkingZone, "deletedAt", LocalDateTime.now());
                ReflectionTestUtils.setField(parkingZone, "status", ParkingZoneStatus.UNAVAILABLE);
                return null;
            }).when(parkingZoneRepository).softDeleteById(anyLong(), any(LocalDateTime.class), any());

            // when
            parkingZoneWriter.deleteParkingZone(parkingZone.getId(),  LocalDateTime.now());

            // then
            verify(parkingZoneRepository).softDeleteById(eq(parkingZone.getId()), any(LocalDateTime.class), eq(ParkingZoneStatus.UNAVAILABLE));
            assertThat(ReflectionTestUtils.getField(parkingZone, "deletedAt")).isNotNull();
        }
    }
}