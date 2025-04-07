package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

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

    @Test
    void 예약_단건_조회_테스트() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Reservation reservation = Reservation.builder()
                .user(user)
                .build();

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

        // when
        Reservation result = reservationReader.findReservation(userId, reservationId);

        // then
        assertNotNull(result);
        assertEquals(reservation, result);
    }

    @Test
    void 예약_단건_조회_시_본인이_한_예약이_아닐_경우_예외() {
        // given
        Long userId = 1L;
        Long differentUserId = 2L;
        Long reservationId = 1L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
        User differentUser = User.builder().build();
        ReflectionTestUtils.setField(differentUser, "id", differentUserId);

        Reservation reservation = Reservation.builder()
                .user(differentUser)
                .build();

        given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> reservationReader.findReservation(userId, reservationId));
        assertEquals(ReservationErrorCode.NOT_MY_RESERVATION, exception.getErrorCode());
    }

    @Test
    void parking_zone_에_대한_예약_내역_리스트_조회_테스트() {
        // given
        Long parkingZoneId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        Reservation reservation = Reservation.builder().build();

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));

        given(reservationRepository.existsByParkingZoneId(anyLong())).willReturn(true);
        given(reservationRepository.findByParkingZoneId(anyLong(), any(PageRequest.class))).willReturn(reservationPage);

        // when
        Page<Reservation> result = reservationReader.findOwnerReservations(parkingZoneId, pageable);

        // then
        assertNotNull(result);
        assertEquals(reservation, result.getContent().get(0));
    }

    @Test
    void parking_zone_에_대한_예약_내역_리스트_조회_시_빈_페이지_전달() {
        // given
        Long parkingZoneId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(reservationRepository.existsByParkingZoneId(anyLong())).willReturn(false);

        // when
        Page<Reservation> result = reservationReader.findOwnerReservations(parkingZoneId, pageable);

        // then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}