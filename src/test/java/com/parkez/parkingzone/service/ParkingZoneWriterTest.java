package com.parkez.parkingzone.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingZoneWriterTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private ParkingLot parkingLot;

    @InjectMocks
    private ParkingZoneWriter parkingZoneWriter;

    private ParkingZoneCreateRequest request;

    @BeforeEach
    void setUp() {
        request = ParkingZoneCreateRequest.builder()
                .parkingLotId(1L)
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    @Test
    void ParkingZone을_생성할_수_있다() {

        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .build();

        // when
        when(parkingZoneRepository.save(any(ParkingZone.class))).thenReturn(savedParkingZone);

        ParkingZone result = parkingZoneWriter.createParkingZone(request, parkingLot);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getImageUrl()).isEqualTo(request.getImageUrl());
        assertThat(result.getStatus()).isEqualTo(ParkingZoneStatus.AVAILABLE);

        Mockito.verify(parkingZoneRepository, Mockito.times(1)).save(any(ParkingZone.class));
    }
}