package com.parkez.reservation.service;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationReaderTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationReader reservationReader;

    @Test
    void 내_예약_리스트_조회_테스트() {
        // given
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        Reservation reservation = Reservation.builder().build();

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));
        given(reservationRepository.existsByUserId(anyLong())).willReturn(true);
        given(reservationRepository.findByUserId(anyLong(), any(PageRequest.class))).willReturn(reservationPage);

        // when
        Page<Reservation> result = reservationReader.findMyReservations(userId, pageable);

        // then
        assertNotNull(result);
        assertEquals(reservationPage.getContent(), result.getContent());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void 내_예약_리스트_조회_시_빈_리스트_조회_테스트() {
        // given
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(reservationRepository.existsByUserId(anyLong())).willReturn(false);

        // when
        Page<Reservation> result = reservationReader.findMyReservations(userId, pageable);

        // then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

}