package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneReaderTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @InjectMocks
    private ParkingZoneReader parkingZoneReader;

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .name("Main Parking Lot")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    private ParkingZone getActiveByParkingZoneId1() {
        ParkingLot parkingLot = getParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        ReflectionTestUtils.setField(parkingZone, "deletedAt", null);
        return parkingZone;
    }

    private ParkingZone getActiveByParkingZoneId2() {
        ParkingLot parkingLot = getParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("B구역")
                .imageUrl("http://example.com/image2.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 2L);
        ReflectionTestUtils.setField(parkingZone, "deletedAt", null);
        return parkingZone;
    }

    @Nested
    class GetParkingZones {
        @Test
        void 주차공간_다건조회_특정_주차장에_대한_주차공간을_정상적으로_디건조회할_수_있다() {
            // given
            ParkingLot parkingLot = getParkingLot();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("modifiedAt").descending());

            ParkingZone parkingZone1 = getActiveByParkingZoneId1();
            ParkingZone parkingZone2 = getActiveByParkingZoneId2();

            Page<ParkingZone> mockPage = new PageImpl<>(List.of(parkingZone1, parkingZone2), pageable, 2);

            when(parkingZoneRepository.findAllByParkingLotIdOrderByModifiedAtDesc(any(Pageable.class), eq(parkingLot.getId())))
                    .thenReturn(mockPage);

            // when
            Page<ParkingZoneResponse> result = parkingZoneReader.getParkingZones(1, 10, parkingLot.getId());

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .contains(tuple(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE),
                            tuple(2L,1L, "B구역","http://example.com/image2.jpg", ParkingZoneStatus.AVAILABLE));
        }
    }

    @Nested
    class GetParkingZone {
        @Test
        void 주차공간_단건조회_특정_주차공간을_정상적으로_단건조회할_수_있다() {
            // given
            ParkingZone parkingZone = getActiveByParkingZoneId1();

            when(parkingZoneRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(parkingZone));

            // when
            ParkingZone result = parkingZoneReader.getActiveByParkingZoneId(1L);

            // then
            assertThat(result).extracting("id","parkingLotId", "name","imageUrl", "status")
                    .containsExactly(1L,1L,"A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        }

        @Test
        void 주차공간_단건조회_존재하지_않는_주차공간을_조회하면_예외가_발생한다() {
            // given
            Long parkingZoneId = -1L;
            when(parkingZoneRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> parkingZoneReader.getActiveByParkingZoneId(parkingZoneId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND.getDefaultMessage());
        }
    }

    @Nested
    class isOwnedParkingZone {
        @Test
        void 주차공간_소유자_본인확인_특정_주차공간의_소유자_본인인지_확인한다() {
            // given
            Long parkingZoneId = 1L;
            Long ownerId = 1L;
            given(parkingZoneRepository.existsByIdAndOwnerId(anyLong(),anyLong())).willReturn(true);

            // when
            boolean result = parkingZoneReader.isOwnedParkingZone(parkingZoneId, ownerId);

            // then
            assertThat(result).isTrue();
        }
    }
}
