package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationWriterTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationWriter reservationWriter;

    @Test
    void 예약_생성_테스트() {
        // given
        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 2L);

        String parkingLotName = "test";

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name(parkingLotName)
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);

        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);

        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

        long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
        BigDecimal price = parkingZone.getParkingLot().getPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLotName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();

        given(reservationRepository.existsReservation(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

        // when
        Reservation result = reservationWriter.createReservation(user, parkingZone, parkingLotName, startDateTime, endDateTime, price);

        // then
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(parkingZone, result.getParkingZone());
        assertEquals(parkingLotName, result.getParkingLotName());
        assertEquals(BigDecimal.valueOf(2000), result.getPrice());
    }

    @Test
    void 예약_생성_시_예약이_이미_존재할_경우_예외() {
        // given
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 2L);

        String parkingLotName = "test";

        ParkingZone parkingZone = ParkingZone.builder().build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);

        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

        given(reservationRepository.existsReservation(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .willReturn(true);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> reservationWriter.createReservation(user, parkingZone, parkingLotName, startDateTime, endDateTime, BigDecimal.valueOf(2000)));
        assertNotNull(exception);
        assertEquals(ReservationErrorCode.ALREADY_RESERVED, exception.getErrorCode());
    }
}