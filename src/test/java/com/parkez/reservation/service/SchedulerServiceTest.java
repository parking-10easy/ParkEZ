package com.parkez.reservation.service;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private SchedulerService schedulerService;

    private static Reservation getReservation(Long id) {
        Reservation reservation = Reservation.builder().build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    @Nested
    class ExpireReservation {

        @Test
        void 예약_생성_후_10분이_지나도_상태가_PENDING인_예약들의_상태를_PAYMENT_EXPIRED_으로_변경_테스트() {
            // given
            Long reservationId = 1L;

            Reservation reservation = getReservation(reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);
            ReflectionTestUtils.setField(reservation, "createdAt", LocalDateTime.now().minusMinutes(11));

            List<Reservation> expiredToReservation = List.of(reservation);

            given(reservationRepository.findReservationsToExpire(any(LocalDateTime.class))).willReturn(expiredToReservation);

            // when
            schedulerService.expire();

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAYMENT_EXPIRED);
            verify(reservationRepository).saveAll(expiredToReservation);
        }
    }

}