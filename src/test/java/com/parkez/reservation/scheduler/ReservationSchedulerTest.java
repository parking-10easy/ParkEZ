package com.parkez.reservation.scheduler;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.service.ReservationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationSchedulerTest {

    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ReservationScheduler schedulerService;

    @Nested
    class ExpireReservation {

        @Test
        void expireReservation_메서드_실행() {
            // given
            doNothing().when(reservationService).expireReservation();

            // when
            schedulerService.expire();

            // then
            verify(reservationService).expireReservation();
        }
    }

}