package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneReaderTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @InjectMocks
    private ParkingZoneReader parkingZoneReader;

    private ParkingLot parkingLot;

    @BeforeEach
    void setUp() {
        parkingLot = ParkingLot.builder()
                .name("Test Parking Lot")
                .build();

        ReflectionTestUtils.setField(parkingLot, "id", 1L);
    }

    @Test
    void 주차공간을_정상적으로_다건_조회한다() {
        // given
        Long parkingLotId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("modifiedAt").descending());

        ParkingZone parkingZone1 = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .build();

        ParkingZone parkingZone2 = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name("B구역")
                .imageUrl("http://example.com/image2.jpg")
                .build();

        Page<ParkingZone> mockPage = new PageImpl<>(List.of(parkingZone1, parkingZone2), pageable, 2);

        when(parkingZoneRepository.findAllOrderByModifiedAt(any(Pageable.class), eq(parkingLotId)))
                .thenReturn(mockPage);

        // when
        Page<ParkingZoneResponse> result = parkingZoneReader.getParkingZones(1, 10, parkingLotId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("A구역");
        assertThat(result.getContent().get(1).getName()).isEqualTo("B구역");

        verify(parkingZoneRepository, times(1)).findAllOrderByModifiedAt(any(Pageable.class), eq(parkingLotId));
    }

    @Test
    void 주차공간을_정상적으로_단건_조회한다() {
        // given
        Long parkingLotId = 1L;

        ParkingZone parkingZone1 = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .build();

        ParkingZone parkingZone2 = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name("B구역")
                .imageUrl("http://example.com/image2.jpg")
                .build();

        ReflectionTestUtils.setField(parkingZone1, "id", 1L);
        ReflectionTestUtils.setField(parkingZone2, "id", 2L);
        ReflectionTestUtils.setField(parkingZone1, "deletedAt", null);
        ReflectionTestUtils.setField(parkingZone2, "deletedAt", null);

        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(parkingZone1));
        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(parkingZone2));

        // when
        ParkingZone result1 = parkingZoneReader.getParkingZone(1L);
        ParkingZone result2 = parkingZoneReader.getParkingZone(2L);

        // then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getId()).isEqualTo(1L);
        assertThat(result2.getId()).isEqualTo(2L);
        assertThat(result1.getName()).isEqualTo("A구역");
        assertThat(result2.getName()).isEqualTo("B구역");
    }

    @Test
    void 존재하지_않는_주차공간을_조회하면_예외가_발생한다() {
        // given
        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ParkingEasyException.class,
                () -> parkingZoneReader.getParkingZone(99L),
                "해당 주차공간을 찾을 수 없습니다.");
    }
}
