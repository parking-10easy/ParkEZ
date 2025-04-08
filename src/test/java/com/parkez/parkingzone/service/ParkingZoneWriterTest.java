package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingZoneWriterTest {

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private ParkingLot parkingLot;

    @InjectMocks
    private ParkingZoneWriter parkingZoneWriter;

    private ParkingZoneCreateRequest createRequest;
    private ParkingZoneUpdateRequest updateRequest;
    private ParkingZoneUpdateStatusRequest updateStatusRequest;
    private ParkingZoneUpdateImageRequest updateImageRequest;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void ParkingZone을_생성할_수_있다() {
        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .build();

        when(parkingZoneRepository.save(any(ParkingZone.class))).thenReturn(savedParkingZone);

        // when
        ParkingZone result = parkingZoneWriter.createParkingZone(createRequest, parkingLot);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        assertThat(result.getImageUrl()).isEqualTo(createRequest.getImageUrl());
        assertThat(result.getStatus()).isEqualTo(ParkingZoneStatus.AVAILABLE);

        Mockito.verify(parkingZoneRepository, Mockito.times(1)).save(any(ParkingZone.class));
    }

    @Test
    void ParkingZone을_수정할_수_있다() {
        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .build();

        ReflectionTestUtils.setField(savedParkingZone, "id", 1L);

        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(savedParkingZone));

        // when
        parkingZoneWriter.updateParkingZone(1L, updateRequest);

        // then
        assertThat(savedParkingZone.getName()).isEqualTo(updateRequest.getName());

        Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
    }

    @Test
    void 존재하지_않는_주차공간을_수정하면_예외가_발생한다() {
        // given
        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(-1L)).thenReturn(Optional.empty());

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> parkingZoneWriter.updateParkingZone(-1L, updateRequest));

        assertThat(exception.getErrorCode()).isEqualTo(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
    }

    @Test
    void ParkingZone의_상태를_변경할_수_있다() {
        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .build();

        ReflectionTestUtils.setField(savedParkingZone, "id", 1L);

        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(savedParkingZone));

        // when
        parkingZoneWriter.updateParkingZoneStatus(1L, updateStatusRequest);

        // then
        assertThat(savedParkingZone.getStatus()).isEqualTo(updateStatusRequest.getStatus());

        Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
    }

    @Test
    void 존재하지_않는_주차공간의_상태를_변경하면_예외가_발생한다() {
        // given
        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(-1L)).thenReturn(Optional.empty());

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> parkingZoneWriter.updateParkingZoneStatus(-1L, updateStatusRequest));

        assertThat(exception.getErrorCode()).isEqualTo(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
    }

    @Test
    void ParkingZone의_이미지를_수정할_수_있다() {
        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .build();

        ReflectionTestUtils.setField(savedParkingZone, "id", 1L);

        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(savedParkingZone));

        // when
        parkingZoneWriter.updateParkingZoneImage(1L, updateImageRequest);

        // then
        assertThat(savedParkingZone.getImageUrl()).isEqualTo(updateImageRequest.getImageUrl());

        Mockito.verify(parkingZoneRepository, Mockito.never()).save(any(ParkingZone.class));
    }

    @Test
    void 존재하지_않는_주차공간의_이미지를_수정하면_예외가_발생한다() {
        // given
        when(parkingZoneRepository.findByIdAndDeletedAtIsNull(-1L)).thenReturn(Optional.empty());

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> parkingZoneWriter.updateParkingZoneImage(-1L, updateImageRequest));

        assertThat(exception.getErrorCode()).isEqualTo(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
    }

    @Test
    void ParkingZone을_삭제할_수_있다() {
        // given
        ParkingZone savedParkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(createRequest.getName())
                .imageUrl(createRequest.getImageUrl())
                .build();

        ReflectionTestUtils.setField(savedParkingZone, "id", 1L);

        when(parkingZoneRepository.existsByIdAndDeletedAtIsNull(1L)).thenReturn(true);
        doAnswer(invocation -> {
            ReflectionTestUtils.setField(savedParkingZone, "deletedAt", LocalDateTime.now());
            return null;
        }).when(parkingZoneRepository).softDeleteById(eq(1L), any(LocalDateTime.class));

        // when
        parkingZoneWriter.deleteParkingZone(1L);

        // then
        verify(parkingZoneRepository).softDeleteById(eq(1L), any(LocalDateTime.class));
        assertThat(ReflectionTestUtils.getField(savedParkingZone, "deletedAt")).isNotNull();
    }

    @Test
    void 존재하지_않는_주차공간을_삭제하면_예외가_발생한다() {
        // given
        when(parkingZoneRepository.existsByIdAndDeletedAtIsNull(-1L)).thenReturn(false);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> parkingZoneWriter.deleteParkingZone(-1L));

        assertThat(exception.getErrorCode()).isEqualTo(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
    }

}