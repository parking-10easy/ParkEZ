package com.parkez.parkingLot;


import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        ParkingLot result = parkingLotWriter.createParkingLot(owner,request);

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
    void 일반_사용자는_주차장을_등록_할_수없다(){
        ParkingEasyException exception = assertThrows(ParkingEasyException.class, ()-> parkingLotWriter.createParkingLot(user,request));
        assertEquals(ParkingLotErrorCode.NOT_OWNER, exception.getErrorCode());
    }
}
