package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.dto.response.OwnerReservationResponse;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.review.service.ReviewReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationReader reservationReader;
    @Mock
    private ReservationWriter reservationWriter;
    @Mock
    private UserReader userReader;
    @Mock
    private ParkingZoneReader parkingZoneReader;
    @Mock
    private ReviewReader reviewReader;
    @InjectMocks
    private ReservationService reservationService;

    private AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.USER)
                .nickname("test")
                .build();
    }

    private AuthUser createAuthOwner(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.OWNER)
                .nickname("test")
                .build();
    }

    private User createUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User createOwner(Long id) {
        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", id);
        return owner;
    }

    private ParkingLot createParkingLot(Long id, User owner) {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name("test")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", id);
        return parkingLot;
    }

    private ParkingZone createParkingZone(Long id, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", id);
        return parkingZone;
    }

    private Reservation createReservation(Long id, User user, ParkingZone parkingZone, ReservationRequest request, BigDecimal price) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.extractParkingLotName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .price(price)
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
    }

    private ReservationRequest createRequest(Long id) {
        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", id);
        ReflectionTestUtils.setField(request, "startDateTime", LocalDateTime.now());
        return request;
    }

    @Nested
    class createReservation {

        @Test
        void 예약_생성_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId);
            ReflectionTestUtils.setField(request, "endDateTime", LocalDateTime.now().plusHours(1));

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
            BigDecimal price = parkingZone.extractParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

            Reservation reservation = createReservation(reservationId, user, parkingZone, request, price);

            given(userReader.getActiveById(anyLong())).willReturn(user);
            given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);
            given(reservationWriter.createReservation(any(User.class), any(ParkingZone.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any(BigDecimal.class)))
                    .willReturn(reservation);

            // when
            MyReservationResponse result = reservationService.createReservation(authUser, request);

            // then
            assertNotNull(result);
            assertEquals(reservationId, result.getReservationId());
            assertEquals(userId, result.getUserId());
            assertEquals(parkingZoneId, result.getParkingZoneId());
            assertEquals(BigDecimal.valueOf(2000), result.getPrice());
            assertFalse(result.isReviewWritten());
        }

        @Test
        void 입력_시간_오류_예외() {
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
                    () -> reservationService.createReservation(userId, request));
            assertNotNull(exception);
            assertEquals(ReservationErrorCode.NOT_VALID_REQUEST_TIME, exception.getErrorCode());
        }
    }

    @Nested
    class getReservationsByUserId {

        @Test
        void 예약_리스트_조회_테스트() {
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
                    .parkingLotName(parkingZone.extractParkingLotName())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            Reservation reviewedReservation = Reservation.builder()
                    .user(user)
                    .parkingZone(parkingZone)
                    .parkingLotName(parkingZone.extractParkingLotName())
                    .build();
            ReflectionTestUtils.setField(reviewedReservation, "id", 2L); // 리뷰 작성된 예약

            ReservationWithReviewDto dto1 = new ReservationWithReviewDto(reservation, false);
            ReservationWithReviewDto dto2 = new ReservationWithReviewDto(reviewedReservation, true);

            PageRequest pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            List<ReservationWithReviewDto> content = List.of(dto1, dto2);
            Page<ReservationWithReviewDto> pageDto = new PageImpl<>(content, pageable, content.size());

            given(reservationReader.findMyReservations(anyLong(), any(PageRequest.class))).willReturn(pageDto);

            // when
            Page<MyReservationResponse> result = reservationService.getMyReservations(userId, page, size);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getContent().get(0).getReservationId());
            assertFalse(result.getContent().get(0).isReviewWritten());
            assertEquals(2L, result.getContent().get(1).getReservationId());
            assertTrue(result.getContent().get(1).isReviewWritten());
            assertEquals(2, result.getTotalElements());
        }

        @Test
        void 정상적이지_않은_page_기입_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            int page = 0;
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
                    .parkingLotName(parkingZone.extractParkingLotName())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L);

            Reservation reviewedReservation = Reservation.builder()
                    .user(user)
                    .parkingZone(parkingZone)
                    .parkingLotName(parkingZone.extractParkingLotName())
                    .build();
            ReflectionTestUtils.setField(reviewedReservation, "id", 2L); // 리뷰 작성된 예약

            ReservationWithReviewDto dto1 = new ReservationWithReviewDto(reservation, false);
            ReservationWithReviewDto dto2 = new ReservationWithReviewDto(reviewedReservation, true);

            PageRequest pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            List<ReservationWithReviewDto> content = List.of(dto1, dto2);
            Page<ReservationWithReviewDto> pageDto = new PageImpl<>(content, pageable, content.size());

            given(reservationReader.findMyReservations(anyLong(), any(PageRequest.class))).willReturn(pageDto);

            // when
            Page<MyReservationResponse> result = reservationService.getMyReservations(userId, page, size);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getContent().get(0).getReservationId());
            assertFalse(result.getContent().get(0).isReviewWritten());
            assertEquals(2L, result.getContent().get(1).getReservationId());
            assertTrue(result.getContent().get(1).isReviewWritten());
            assertEquals(2, result.getTotalElements());
        }
    }

    @Nested
    class getReservationById {

        @Test
        void 예약_단건_조회_테스트() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", userId);

            ParkingZone parkingZone = ParkingZone.builder()
                    .build();
            ReflectionTestUtils.setField(parkingZone, "id", 1L);

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .parkingZone(parkingZone)
                    .build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            boolean isReviewWritten = false;

            given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);
            given(reviewReader.isReviewWritten(anyLong())).willReturn(isReviewWritten);

            // when
            MyReservationResponse result = reservationService.getMyReservation(reservationId, userId);

            // then
            assertNotNull(result);
            assertEquals(reservationId, result.getReservationId());
        }
    }

    @Nested
    class getReservationsByParkingZoneId {

        @Test
        void 예약_내역_리스트_조회_테스트() {
            // given
            Long userId = 1L;
            Long parkingZoneId = 1L;
            int page = 1;
            int size = 10;

            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", userId);

            ParkingLot parkingLot = ParkingLot.builder()
                    .owner(user)
                    .name("test")
                    .build();

            ParkingZone parkingZone = ParkingZone.builder()
                    .parkingLot(parkingLot)
                    .build();
            ReflectionTestUtils.setField(parkingZone, "id", 1L);

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .parkingZone(parkingZone)
                    .parkingLotName(parkingZone.getParkingLot().getName())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L); // 리뷰 작성된 예약

            Page<Reservation> pageMyReservations = new PageImpl<>(List.of(reservation));

            given(parkingZoneReader.existsById(anyLong())).willReturn(true);
            given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);
            given(reservationReader.findOwnerReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);

            // when
            Page<OwnerReservationResponse> result = reservationService.getOwnerReservations(userId, parkingZoneId, page, size);

            // then
            assertNotNull(result);
            assertEquals(parkingZoneId, result.getContent().get(0).getParkingZoneId());
        }

        @Test
        void 정상적이지_않은_page_기입_테스트() {
            // given
            Long userId = 1L;
            Long parkingZoneId = 1L;
            int page = 0;
            int size = 10;

            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", userId);

            ParkingLot parkingLot = ParkingLot.builder()
                    .owner(user)
                    .name("test")
                    .build();

            ParkingZone parkingZone = ParkingZone.builder()
                    .parkingLot(parkingLot)
                    .build();
            ReflectionTestUtils.setField(parkingZone, "id", 1L);

            Reservation reservation = Reservation.builder()
                    .user(user)
                    .parkingZone(parkingZone)
                    .parkingLotName(parkingZone.getParkingLot().getName())
                    .build();
            ReflectionTestUtils.setField(reservation, "id", 1L); // 리뷰 작성된 예약

            Page<Reservation> pageMyReservations = new PageImpl<>(List.of(reservation));

            given(parkingZoneReader.existsById(anyLong())).willReturn(true);
            given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);
            given(reservationReader.findOwnerReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);

            // when
            Page<OwnerReservationResponse> result = reservationService.getOwnerReservations(userId, parkingZoneId, page, size);

            // then
            assertNotNull(result);
            assertEquals(parkingZoneId, result.getContent().get(0).getParkingZoneId());
        }

        @Test
        void 주차공간이_없을_경우_예외() {
            // given
            Long userId = 1L;
            Long parkingZoneId = 1L;
            int page = 1;
            int size = 10;

            given(parkingZoneReader.existsById(anyLong())).willReturn(false);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.getOwnerReservations(userId, parkingZoneId, page, size));
            assertEquals(ReservationErrorCode.NOT_FOUND_PARKING_ZONE, exception.getErrorCode());
        }

        @Test
        void 본인_주차공간이_아닐_경우_예외() {
            // given
            Long userId = 1L;
            Long differentUserId = 2L;
            Long parkingZoneId = 1L;
            int page = 1;
            int size = 10;

            User differentUser = User.builder().build();
            ReflectionTestUtils.setField(differentUser, "id", differentUserId);

            ParkingLot parkingLot = ParkingLot.builder()
                    .owner(differentUser)
                    .build();

            ParkingZone parkingZone = ParkingZone.builder()
                    .parkingLot(parkingLot)
                    .build();

            given(parkingZoneReader.existsById(anyLong())).willReturn(true);
            given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.getOwnerReservations(userId, parkingZoneId, page, size));
            assertEquals(ReservationErrorCode.NOT_MY_PARKING_ZONE, exception.getErrorCode());
        }
    }

    @Nested
    class completeReservation {

        @Test
        void 예약_사용_완료_테스트() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

            given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);
            doNothing().when(reservationWriter).complete(reservation);

            // when
            reservationService.completeReservation(userId, reservationId);

            // then
            verify(reservationWriter, times(1)).complete(reservation);
        }

        @Test
        void 예약의_상태가_CONFIRMED가_아닐_경우_예외() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

            given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.completeReservation(userId, reservationId));
            assertEquals(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS, exception.getErrorCode());
        }
    }

    @Nested
    class cancelReservation {

        @Test
        void 예약_취소_테스트() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            LocalDateTime startDateTime = LocalDateTime.now().plusHours(3);

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);
            ReflectionTestUtils.setField(reservation, "startDateTime", startDateTime);

            given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
            doNothing().when(reservationWriter).cancel(reservation);

            // when
            reservationService.cancelReservation(userId, reservationId);

            // then
            verify(reservationWriter, times(1)).cancel(reservation);
        }

        @Test
        void 예약의_상태가_COMPLETED_일_경우_예외() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.COMPLETED);

            given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.cancelReservation(userId, reservationId));
            assertEquals(ReservationErrorCode.CANT_CANCEL_COMPLETED_RESERVATION, exception.getErrorCode());
        }

        @Test
        void 예약의_상태가_CANCELED_일_경우_예외() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CANCELED);

            given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.cancelReservation(userId, reservationId));
            assertEquals(ReservationErrorCode.CANT_CANCEL_CANCELED_RESERVATION, exception.getErrorCode());
        }

        @Test
        void 남은_시간이_1시간_이내일_경우_예외() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            LocalDateTime startDateTime = LocalDateTime.now();

            Reservation reservation = Reservation.builder().build();
            ReflectionTestUtils.setField(reservation, "id", reservationId);
            ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);
            ReflectionTestUtils.setField(reservation, "startDateTime", startDateTime);

            given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.cancelReservation(userId, reservationId));
            assertEquals(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR, exception.getErrorCode());
        }
    }
}