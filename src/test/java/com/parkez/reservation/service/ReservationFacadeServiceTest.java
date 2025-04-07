package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneQueryService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.review.service.ReviewQueryService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    @Mock
    private ReviewQueryService reviewQueryService;
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

    @Test
    void 내_예약_리스트_조회_테스트() {
        // given
        Long ownerId = 1L;
        Long userId = 2L;
        int page = 1;
        int size = 10;

        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .name("test")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);

        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLot().getName())
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);

        Reservation reviewedReservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLot().getName())
                .build();
        ReflectionTestUtils.setField(reviewedReservation, "id", 2L); // 리뷰 작성된 예약

        Page<Reservation> pageMyReservations = new PageImpl<>(List.of(reservation, reviewedReservation));


        given(reservationReader.findMyReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);
        given(reviewQueryService.findReviewedReservationIds(anyList())).willReturn(Set.of(reviewedReservation.getId()));

        // when
        Page<MyReservationResponse> result = reservationFacadeService.getMyReservations(userId, page, size);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getContent().get(0).getReservationId());
        assertFalse(result.getContent().get(0).isReviewWritten());
        assertEquals(2L, result.getContent().get(1).getReservationId());
        assertTrue(result.getContent().get(1).isReviewWritten());
        assertEquals(2, result.getTotalElements());

    }
}