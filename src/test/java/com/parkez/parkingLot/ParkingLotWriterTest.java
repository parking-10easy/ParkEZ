package com.parkez.parkingLot;


import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.FakeImage;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.service.ParkingLotWriter;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotWriterTest {

    @InjectMocks
    private ParkingLotWriter parkingLotWriter;

    @Mock
    private ParkingLotRepository parkingLotrepository;

    private User owner;
    private User user;
    private ParkingLotRequest request;
    private ParkingLot parkingLot;

    @BeforeEach
    void setUP() {
        owner = User.builder()
                .email("owner@owner.com")
                .nickname("소유주")
                .phone("111-1111-1111")
                .role(UserRole.ROLE_OWNER)
                .build();

        user = User.builder()
                .email("user@user.com")
                .nickname("사용자")
                .phone("222-2222-2222")
                .role(UserRole.ROLE_USER)
                .build();

        request = ParkingLotRequest.builder()
                .name("테스트 주차장")
                .address("테스트 거리")
                .openedAt(LocalTime.of(8, 0))
                .closedAt(LocalTime.of(22, 0))
                .pricePerHour(new BigDecimal("5.00"))
                .description("테스트용 주차장입니다.")
                .quantity(100)
                .build();

        parkingLot = ParkingLot.builder()
                .owner(owner)
                .name(request.getName())
                .address(request.getAddress())
                .openedAt(request.getOpenedAt())
                .closedAt(request.getClosedAt())
                .pricePerHour(request.getPricePerHour())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .build();
    }

    @Test
    void 주차장을_정상적으로_등록한다() {
        when(parkingLotrepository.save(any(ParkingLot.class))).thenReturn(parkingLot);
        ParkingLot result = parkingLotWriter.createParkingLot(owner, request);

        //then
        assertNotNull(result);
        assertEquals(owner, result.getOwner());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getAddress(), result.getAddress());
        assertEquals(request.getOpenedAt(), result.getOpenedAt());
        assertEquals(request.getClosedAt(), result.getClosedAt());
        assertEquals(request.getPricePerHour(), result.getPricePerHour());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getQuantity(), result.getQuantity());
    }

    @Test
    void 사용자는_주차장을_등록_할_수없다() {
        ParkingEasyException exception = assertThrows(ParkingEasyException.class, () -> parkingLotWriter.createParkingLot(user, request));
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }

    @Test
    void 주차장을_수정한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingLotRequest updatedRequest = ParkingLotRequest.builder()
                .name("수정된 주차장")
                .address("수정된 주소")
                .openedAt(LocalTime.of(7, 0))
                .closedAt(LocalTime.of(21, 0))
                .pricePerHour(new BigDecimal("6.00"))
                .description("수정된 설명")
                .quantity(80)
                .build();
        parkingLotWriter.updateParkingLot(owner, parkingLotId, updatedRequest);

        assertEquals("수정된 주차장", parkingLot.getName());
        assertEquals("수정된 주소", parkingLot.getAddress());
        assertEquals(LocalTime.of(7, 0), parkingLot.getOpenedAt());
        assertEquals(LocalTime.of(21, 0), parkingLot.getClosedAt());
        assertEquals(new BigDecimal("6.00"), parkingLot.getPricePerHour());
        assertEquals("수정된 설명", parkingLot.getDescription());
        assertEquals(80, parkingLot.getQuantity());
    }

    @Test
    void 소유자가_아니면_주차장_수정_실패한다(){
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingLotRequest updateRequest = ParkingLotRequest.builder()
                .name("수정된 주차장")
                .address("수정된 주소")
                .openedAt(LocalTime.of(7, 0))
                .closedAt(LocalTime.of(21, 0))
                .pricePerHour(new BigDecimal("6.00"))
                .description("수정된 설명")
                .quantity(80)
                .build();
        ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                parkingLotWriter.updateParkingLot(user, parkingLotId, updateRequest));
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }

    @Test
    void 주차장_상태를_변경한다(){
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingLotStatusRequest statusRequest = ParkingLotStatusRequest.builder()
                .status(ParkingLotStatus.CLOSED)
                .build();
        parkingLotWriter.updateParkingLotStatus(owner, parkingLotId, statusRequest);
        assertEquals(ParkingLotStatus.CLOSED, parkingLot.getStatus());
    }

    @Test
    void 소유자가_아니면_주차장_상태_변경을_실패한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingLotStatusRequest statusRequest = ParkingLotStatusRequest.builder()
                .status(ParkingLotStatus.CLOSED)
                .build();
        ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                parkingLotWriter.updateParkingLotStatus(user, parkingLotId, statusRequest)
        );
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }

    @Test
    void 주차장을_삭제한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        parkingLotWriter.deleteParkingLot(owner, parkingLotId);
        assertNotNull(parkingLot.getDeletedAt());
    }

    @Test
    void 주차장_삭제를_실패한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                parkingLotWriter.deleteParkingLot(user, parkingLotId)
        );
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }

    @Test
    void 주차장_이미지를_수정한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));
        ParkingLotImagesRequest imagesRequest = ParkingLotImagesRequest.builder()
                .imageUrls(List.of(
                        "https://example.com/images/parking_lot_1.jpg",
                        "https://example.com/images/parking_lot_2.jpg"
                ))
                .build();
        parkingLotWriter.updateParkingLotImages(owner, parkingLotId, imagesRequest);
        List<String> updatedImageUrls = parkingLot.getImages().stream()
                .map(FakeImage::getImageUrl)
                .toList();
        assertNotNull(updatedImageUrls);
        assertEquals(2, updatedImageUrls.size());
        assertTrue(updatedImageUrls.contains("https://example.com/images/parking_lot_1.jpg"));
        assertTrue(updatedImageUrls.contains("https://example.com/images/parking_lot_2.jpg"));
    }

    @Test
    void 소유자가_아니면_주차장_이미지_수정을_실패한다() {
        Long parkingLotId = 1L;
        when(parkingLotrepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.ofNullable(parkingLot));

        ParkingLotImagesRequest imagesRequest = ParkingLotImagesRequest.builder()
                .imageUrls(List.of("https://example.com/images/parking_lot_1.jpg"))
                .build();

        ParkingEasyException exception = assertThrows(ParkingEasyException.class, () ->
                parkingLotWriter.updateParkingLotImages(user, parkingLotId, imagesRequest)
        );
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }
}
