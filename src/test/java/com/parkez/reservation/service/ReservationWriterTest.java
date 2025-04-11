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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
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
        void 특정_주차공간에_대한_예약_생성_테스트() {
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

            LocalDateTime startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endDateTime = startDateTime.plusHours(1);

            long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
            BigDecimal price = parkingZone.extractParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

            Reservation reservation = createReservation(reservationId, user, parkingZone, parkingLotName, startDateTime, endDateTime, price);

            given(reservationRepository.existsReservationByConditions(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                    .willReturn(false);
            given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

            // when
            Reservation result = reservationWriter.createReservation(user, parkingZone, parkingLotName, startDateTime, endDateTime, price);

            // then
            assertThat(result)
                    .isNotNull()
                    .extracting("id", "user", "parkingZone", "parkingLotName", "startDateTime", "endDateTime", "price", "status")
                    .isEqualTo(
                            List.of(reservationId, user, parkingZone, parkingLotName, startDateTime, endDateTime, price, ReservationStatus.PENDING)
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_예약이_이미_존재할_경우_ALREADY_RESERVED_예외_처리() {
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

            given(reservationRepository.existsReservationByConditions(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
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
        void 특정_예약_사용_완료_테스트() {
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
        void 특정_예약_취소_테스트() {
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