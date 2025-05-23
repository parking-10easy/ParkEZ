package com.parkez.reservation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.user.domain.entity.User;

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
            BigDecimal price,
            BigDecimal originalPrice,
            BigDecimal discountAmount,
            Long promotionIssueId
    ) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLotName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .promotionIssueId(promotionIssueId)
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
            BigDecimal originalPrice = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal price = originalPrice.subtract(discountAmount);
            Long promotionIssueId = null;

            Reservation reservation = createReservation(reservationId, user, parkingZone, parkingLotName, startDateTime, endDateTime, price,
                originalPrice, discountAmount, promotionIssueId);

            given(reservationRepository.save(any(Reservation.class))).willReturn(reservation);

            // when
            Reservation result = reservationWriter.create(user, parkingZone, startDateTime, endDateTime,originalPrice, discountAmount,price,promotionIssueId);

            // then
            assertThat(result)
                    .isNotNull()
                    .extracting("id", "user", "parkingZone", "parkingLotName", "startDateTime", "endDateTime", "price", "status", "reviewWritten")
                    .isEqualTo(
                            List.of(reservationId, user, parkingZone, parkingLotName, startDateTime, endDateTime, price, ReservationStatus.PENDING, false)
                    );
        }
    }

    @Nested
    class CompleteReservation {

        @Test
        void 특정_예약_사용_완료_테스트() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

            // when
            reservationWriter.complete(reservation);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
            assertThat(reservation.getUseCompletionTime()).isNotNull();
        }
    }

    @Nested
    class CancelReservation {

        @Test
        void 특정_예약_취소_테스트() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

            // when
            reservationWriter.cancel(reservation);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        }
    }

    @Nested
    class updateStatusConfirm {
        @Test
        void 예약_상태를_CONFIRMED로_변경() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

            // when
            reservationWriter.updateStatusConfirm(reservation);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }
    }

    @Nested
    class ExpirePaymentTimeout {

        @Test
        void 결제_시간_초과로_예약_만료_상태로_변경() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

            // when
            reservationWriter.expirePaymentTimeout(reservation);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAYMENT_EXPIRED);
        }
    }

    @Nested
    class ExpireReservation {

        @Test
        void 예약_생성_후_일정_시간_이내_결제_요청을_생성하지_않을_경우_예약_만료() {
            // given
            Long reservationId = 1L;
            LocalDateTime expiredTime = LocalDateTime.now();

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);
            ReflectionTestUtils.setField(reservation, "createdAt", LocalDateTime.now().minusMinutes(11));

            List<Reservation> expiredToReservation = List.of(reservation);

            given(reservationRepository.findReservationsToExpire(any(LocalDateTime.class))).willReturn(expiredToReservation);

            // when
            reservationWriter.expire(expiredTime);

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAYMENT_EXPIRED);
            verify(reservationRepository).saveAll(expiredToReservation);
        }
    }
}