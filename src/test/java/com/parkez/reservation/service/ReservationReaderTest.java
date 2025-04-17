package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;

@ExtendWith(MockitoExtension.class)
class ReservationReaderTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationReader reservationReader;

    private static User createUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static ParkingZone createParkingZone(Long id) {
        ParkingZone parkingZone = ParkingZone.builder().build();
        ReflectionTestUtils.setField(parkingZone, "id", id);
        return parkingZone;
    }

    private static Reservation createReservation(Long id, User user) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    @Nested
    class GetReservationsByUserId {

        @Test
        void 특정_사용자의_예약_리스트_조회_테스트() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            User user = createUser(userId);

            Reservation reservation = createReservation(reservationId, user);
            ReservationWithReviewDto dto = new ReservationWithReviewDto(reservation, true);

            Page<ReservationWithReviewDto> pageDto = new PageImpl<>(List.of(dto));
            given(reservationRepository.existsByUser_Id(anyLong())).willReturn(true);
            given(reservationRepository.findAllWithReviewByUser_Id(anyLong(), any(PageRequest.class))).willReturn(pageDto);

            // when
            Page<ReservationWithReviewDto> result = reservationReader.findMyReservations(userId, pageable);

            // then
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(ReservationWithReviewDto::reservation, ReservationWithReviewDto::reviewWritten)
                    .contains(
                            tuple(reservation, true)
                    );
        }

        @Test
        void 특정_사용자의_예약_리스트_조회_시_예약이_존재하지_않을_경우_빈_페이지_전달() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            given(reservationRepository.existsByUser_Id(anyLong())).willReturn(false);

            // when
            Page<ReservationWithReviewDto> result = reservationReader.findMyReservations(userId, pageable);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .isEmpty();
        }
    }

    @Nested
    class GetReservationByUserId {

        @Test
        void 특정_사용자의_특정_예약_조회_테스트() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            User user = createUser(userId);

            Reservation reservation = createReservation(reservationId, user);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            // when
            Reservation result = reservationReader.findMyReservation(userId, reservationId);

            // then
            assertThat(result)
                    .isNotNull()
                    .isEqualTo(reservation);
        }

        @Test
        void 특정_사용자의_특정_예약_조회_시_예약이_존재하지_않을_경우_NOT_FOUND_RESERVATION_예외_처리() {
            // given
            Long userId = 1L;
            Long reservationId = -1L;

            given(reservationRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationReader.findMyReservation(userId, reservationId));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_FOUND_RESERVATION);
        }

        @Test
        void 특정_사용자의_특정_예약_단건_조회_시_본인이_한_예약이_아닐_경우_NOT_MY_RESERVATION_예외_처리() {
            // given
            Long userId = 1L;
            Long differentUserId = 2L;
            Long reservationId = 1L;

            User differentUser = createUser(differentUserId);

            Reservation reservation = createReservation(reservationId, differentUser);

            given(reservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationReader.findMyReservation(userId, reservationId));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_MY_RESERVATION);
        }
    }

    @Nested
    class GetReservationsByParkingZoneId {

        @Test
        void 특정_주차공간에_대한_예약_내역_리스트_조회_테스트() {
            // given
            Long userId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            User user = createUser(userId);

            ParkingZone parkingZone = createParkingZone(parkingZoneId);

            Reservation reservation = createReservation(reservationId, user);
            ReflectionTestUtils.setField(reservation, "parkingZone", parkingZone);

            Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));

            given(reservationRepository.existsByParkingZone_Id(anyLong())).willReturn(true);
            given(reservationRepository.findAllByParkingZone_Id(anyLong(), any(PageRequest.class))).willReturn(reservationPage);

            // when
            Page<Reservation> result = reservationReader.findOwnerReservations(parkingZoneId, pageable);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .extracting("id", "user", "parkingZone")
                    .contains(
                            tuple(reservationId, user, parkingZone)
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_내역_리스트_조회_시_예약이_존재하지_않을_경우_빈_페이지_전달() {
            // given
            Long parkingZoneId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);

            given(reservationRepository.existsByParkingZone_Id(anyLong())).willReturn(false);

            // when
            Page<Reservation> result = reservationReader.findOwnerReservations(parkingZoneId, pageable);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .isEmpty();
        }
    }

}