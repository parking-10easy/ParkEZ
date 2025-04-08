package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private ParkingLot parkingLot;
    private ParkingZone parkingZone;
    private ParkingZoneCreateRequest createRequest;
    private ParkingZoneUpdateRequest updateRequest;
    private ParkingZoneUpdateStatusRequest updateStatusRequest;
    private ParkingZoneUpdateImageRequest updateImageRequest;
    private ParkingZoneCreateResponse createResponse;
    private ParkingZoneResponse response;

    @BeforeEach
    void setUp() {
        parkingLot = ParkingLot.builder()
                .name("Main Parking Lot")
                .build();

        ReflectionTestUtils.setField(parkingLot, "id", 1L);

        parkingZone = ParkingZone.builder()
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);

        createRequest = ParkingZoneCreateRequest.builder()
                .parkingLotId(1L)
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .build();

        updateRequest = ParkingZoneUpdateRequest.builder()
                .name("A구역 수정")
                .build();

        updateStatusRequest = ParkingZoneUpdateStatusRequest.builder()
                .status(ParkingZoneStatus.UNAVAILABLE)
                .build();

        updateImageRequest = ParkingZoneUpdateImageRequest.builder()
                .imageUrl("http://example.com/image수정.jpg")
                .build();

        createResponse = new ParkingZoneCreateResponse(1L,1L, "A구역","http://example.com/image.jpg", ParkingZoneStatus.AVAILABLE);
        response = new ParkingZoneResponse(1L,1L, "A구역 수정","http://example.com/image수정.jpg", ParkingZoneStatus.AVAILABLE);
    }

    @Test
    void 주차_구역을_정상적으로_생성할_수_있다() {
        // given
        when(parkingLotReader.getParkingLot(1L)).thenReturn(parkingLot);
        when(parkingZoneWriter.createParkingZone(createRequest, parkingLot)).thenReturn(parkingZone);

        // when
        ParkingZoneCreateResponse result = parkingZoneService.createParkingZone(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("A구역");

        verify(parkingZoneWriter, times(1)).createParkingZone(createRequest, parkingLot);
    }

    @Test
    void 주차장이_존재하지_않으면_예외가_발생한다() {
        // given
        doThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND))
                .when(parkingLotReader).getParkingLot(anyLong());

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> parkingZoneService.createParkingZone(createRequest));

        assertThat(exception.getErrorCode()).isEqualTo(ParkingLotErrorCode.NOT_FOUND);

        verify(parkingLotReader, times(1)).getParkingLot(anyLong());
        verify(parkingZoneWriter, never()).createParkingZone(any(), any());
    }
}