package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;

@ExtendWith(MockitoExtension.class)
class ReservationWriterTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationWriter reservationWriter;

    private static User createUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static ParkingLot createParkingLot(Long id, User owner, String parkingLotName) {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name(parkingLotName)
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", id);
        return parkingLot;
    }

    private static ParkingZone createParkingZone(Long id, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", id);
        return parkingZone;
    }

    private static Reservation createReservation(
            Long id,
            User user,
            ParkingZone parkingZone,
            String parkingLotName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            BigDecimal price
    ) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLotName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private static Reservation getReservation(Long id) {
        Reservation reservation = Reservation.builder().build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    @Nested
    class CreateReservation {

        @Test
        void 예약_생성_테스트() {
            // given
            Long userId = 1L;
            Long ownerId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;
            String parkingLotName = "test";

            User user = createUser(userId);
            User owner = createUser(ownerId);

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner, parkingLotName);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

            long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
            BigDecimal price = parkingZone.extractParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

            Reservation reservation = createReservation(reservationId, user, parkingZone, parkingLotName, startDateTime, endDateTime, price);

            given(reservationRepository.existsReservation(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                    .willReturn(false);
            given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

            // when
            Reservation result = reservationWriter.createReservation(user, parkingZone, parkingLotName, startDateTime, endDateTime, price);

            // then
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(user, result.getUser()),
                    () -> assertEquals(parkingZone, result.getParkingZone()),
                    () -> assertEquals(parkingLotName, result.getParkingLotName()),
                    () -> assertEquals(BigDecimal.valueOf(2000), result.getPrice())
            );
        }

        @Test
        void 예약이_이미_존재할_경우_예외() {
            // given
            Long userId = 1L;
            Long ownerId = 2L;
            String parkingLotName = "test";
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            BigDecimal price = BigDecimal.valueOf(2000);

            User user = createUser(userId);
            User owner = createUser(ownerId);

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner, parkingLotName);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime endDateTime = LocalDateTime.now().plusHours(1);

            given(reservationRepository.existsReservation(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                    .willReturn(true);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationWriter.createReservation(user, parkingZone, parkingLotName, startDateTime, endDateTime, price));
            assertNotNull(exception);
            assertEquals(ReservationErrorCode.ALREADY_RESERVED, exception.getErrorCode());
        }
    }

    @Nested
    class completeReservation {

        @Test
        void 예약_사용_완료_테스트() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

            // when
            reservationWriter.complete(reservation);

            // then
            assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        }
    }

    @Nested
    class cancelReservation {

        @Test
        void 예약_취소_테스트() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

            // when
            reservationWriter.cancel(reservation);

            // then
            assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
        }
    }
}