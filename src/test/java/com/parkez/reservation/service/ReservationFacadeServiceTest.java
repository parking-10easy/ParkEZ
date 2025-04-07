package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneQueryService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeServiceTest {

    @Mock
    private ReservationReader reservationReader;
    @Mock
    private ReservationWriter reservationWriter;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private ParkingZoneQueryService parkingZoneQueryService;
    @InjectMocks
    private ReservationFacadeService reservationFacadeService;

    @Test
    void 예약_생성_테스트() {
        // given
        Long ownerId = 1L;
        Long userId = 2L;
        Long parkingLotId = 1L;
        Long parkingZoneId = 1L;
        Long reservationId = 1L;

        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", parkingZoneId);
        ReflectionTestUtils.setField(request, "startDateTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "endDateTime", LocalDateTime.now().plusHours(1));

        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name("test")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", parkingLotId);

        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", parkingZoneId);

        long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
        BigDecimal price = parkingZone.getParkingLot().getPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLot().getName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .price(price)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        given(userQueryService.findById(anyLong())).willReturn(user);
        given(parkingZoneQueryService.findById(anyLong())).willReturn(parkingZone);
        given(reservationWriter.createReservation(any(User.class), any(ParkingZone.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any(BigDecimal.class)))
                .willReturn(reservation);

        // when
        MyReservationResponse result = reservationFacadeService.createReservation(userId, request);

        // then
        assertNotNull(result);
        assertEquals(reservationId, result.getReservationId());
        assertEquals(userId, result.getUserId());
        assertEquals(parkingZoneId, result.getParkingZoneId());
        assertEquals(BigDecimal.valueOf(2000), result.getPrice());
        assertFalse(result.isReviewWritten());
    }

    @Test
    void 예약_생성_시_입력_시간_오류_예외() {
        // given
        Long ownerId = 1L;
        Long userId = 2L;
        Long parkingLotId = 1L;
        Long parkingZoneId = 1L;

        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", parkingZoneId);
        ReflectionTestUtils.setField(request, "startDateTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "endDateTime", LocalDateTime.now().minusHours(1));

        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name("test")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", parkingLotId);

        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", parkingZoneId);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> reservationFacadeService.createReservation(userId, request));
        assertNotNull(exception);
        assertEquals(ReservationErrorCode.NOT_VALID_REQUEST_TIME, exception.getErrorCode());
    }
}