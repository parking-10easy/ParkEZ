package com.parkez.parkingLot;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkinglot.service.ParkingLotService;
import com.parkez.parkinglot.service.ParkingLotWriter;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotServiceTest {

    @InjectMocks
    private ParkingLotService parkingLotService;

    @Mock
    private ParkingLotWriter parkingLotWriter;

    @Mock
    private ParkingLotReader parkingLotReader;

    private User owner;

    private ParkingLot dummyParkingLot;

    private ParkingLotRequest parkingLotRequest;
    private ParkingLotResponse expectedResponse;

    private ParkingLotSearchRequest searchRequest;
    private ParkingLotSearchResponse expectedSearchResponse;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .email("owner@owner.com")
                .nickname("소유주")
                .phone("111-1111-1111")
                .role(UserRole.ROLE_OWNER)
                .build();
        parkingLotRequest = ParkingLotRequest.builder()
                .name("테스트 주차장")
                .address("테스트 거리")
                .openedAt(LocalTime.of(8, 0))
                .closedAt(LocalTime.of(22, 0))
                .pricePerHour(new BigDecimal("5.00"))
                .description("테스트용 주차장입니다.")
                .quantity(100)
                .build();

        dummyParkingLot = ParkingLot.builder()
                .owner(owner)
                .name(parkingLotRequest.getName())
                .address(parkingLotRequest.getAddress())
                .openedAt(parkingLotRequest.getOpenedAt())
                .closedAt(parkingLotRequest.getClosedAt())
                .pricePerHour(parkingLotRequest.getPricePerHour())
                .description(parkingLotRequest.getDescription())
                .quantity(parkingLotRequest.getQuantity())
                .build();

        searchRequest = new ParkingLotSearchRequest();
        pageable = PageRequest.of(0, 10);
        expectedResponse = ParkingLotResponse.from(dummyParkingLot);
        expectedSearchResponse = ParkingLotSearchResponse.from(dummyParkingLot);
    }

    @Test
    void createParkingLot을_사용하여_주차장을_생성한다() {
        when(parkingLotWriter.createParkingLot(owner, parkingLotRequest)).thenReturn(dummyParkingLot);
        ParkingLotResponse response = parkingLotService.createParkingLot(owner, parkingLotRequest);
        assertNotNull(response);
        assertEquals(expectedResponse.getName(), response.getName());
        //서비스 계층의 메소드가 올바른 의존성(ParkingLotWriter)을 호출하는지 검증
        verify(parkingLotWriter).createParkingLot(owner, parkingLotRequest);
    }

    @Test
    void searchParkingLots를_사용하여_주차장을_다건_조회한다() {
        List<ParkingLotSearchResponse> responses = Arrays.asList(expectedSearchResponse);
        Page<ParkingLotSearchResponse> page = new PageImpl<>(responses, pageable, responses.size());
        when(parkingLotReader.searchParkingLots(searchRequest, pageable)).thenReturn(page);
        Page<ParkingLotSearchResponse> result = parkingLotService.searchParkingLots(searchRequest, pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedSearchResponse.getName(), result.getContent().get(0).getName());
        verify(parkingLotReader).searchParkingLots(searchRequest, pageable);
    }

    @Test
    void searchParkingLot을_사용하여_주차장을_단건_조회한다() {
        when(parkingLotReader.searchParkingLot(anyLong())).thenReturn(dummyParkingLot);
        ParkingLotSearchResponse response = parkingLotService.searchParkingLot(1L);
        assertNotNull(response);
        assertEquals(expectedSearchResponse.getName(), response.getName());
        verify(parkingLotReader).searchParkingLot(1L);
    }

    @Test
    void updateParkingLot을_사용하여_주차장을_수정한다() {
        parkingLotService.updateParkingLot(owner, 1L, parkingLotRequest);
        verify(parkingLotWriter).updateParkingLot(owner, 1L, parkingLotRequest);
    }

    @Test
    void updateParkingLotStatus을_사용하여_주차장의_상태를_변경한다() {
        ParkingLotStatusRequest statusRequest = ParkingLotStatusRequest.builder()
                .status(ParkingLotStatus.CLOSED)
                .build();
        parkingLotService.updateParkingLotStatus(owner, 1L, statusRequest);
        verify(parkingLotWriter).updateParkingLotStatus(owner, 1L, statusRequest);
    }

    @Test
    void deleteParkingLot을_사용하여_주차장을_삭제한다() {
        parkingLotService.deleteParkingLot(owner, 1L);
        verify(parkingLotWriter).deleteParkingLot(owner, 1L);
    }

    @Test
    void updateParkingLotImages을_사용하여_이미지를_수정한다(){
        ParkingLotImagesRequest imagesRequest = ParkingLotImagesRequest.builder()
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .build();
        parkingLotService.updateParkingLotImages(owner, 1L, imagesRequest);
        verify(parkingLotWriter).updateParkingLotImages(owner, 1L, imagesRequest);
    }
}
