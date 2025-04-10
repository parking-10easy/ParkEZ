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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private static AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.USER)
                .nickname("test")
                .build();
    }

    private static AuthUser createAuthOwner(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.OWNER)
                .nickname("test")
                .build();
    }

    private static User createUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static User createOwner(Long id) {
        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", id);
        return owner;
    }

    private static ParkingLot createParkingLot(Long id, User owner) {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(owner)
                .pricePerHour(BigDecimal.valueOf(2000))
                .name("test")
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

    private static Reservation createReservation(Long id, User user, ParkingZone parkingZone, ReservationRequest request, BigDecimal price) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.extractParkingLotName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .price(price)
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private static Reservation getReservation(Long id, User user, ParkingZone parkingZone) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.extractParkingLotName())
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private static ReservationRequest createRequest(Long id) {
        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", id);
        ReflectionTestUtils.setField(request, "startDateTime", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return request;
    }

    @Nested
    class CreateReservation {

        @Test
        void 특정_주차공간에_대한_예약_생성_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId);
            ReflectionTestUtils.setField(request, "endDateTime", request.getStartDateTime().plusHours(1));

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
            assertThat(result)
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten", "startDateTime", "endDateTime", "price")
                    .isEqualTo(
                            List.of(reservationId, userId, parkingZoneId, parkingLot.getName(), false, request.getStartDateTime(), request.getEndDateTime(), price)
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_request_입력_시간_오류의_경우_NOT_VALID_REQUEST_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId);
            ReflectionTestUtils.setField(request, "endDateTime", request.getStartDateTime().minusHours(1));

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(userReader.getActiveById(anyLong())).willReturn(user);
            given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request));
            assertEquals(ReservationErrorCode.NOT_VALID_REQUEST_TIME, exception.getErrorCode());
        }
    }

    @Nested
    class GetReservationsByUserId {

        @Test
        void 특정_사용자의_예약_리스트_조회_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            int page = 1;
            int size = 10;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;
            Long reviewedReservationId = 2L;

            AuthUser authUser = createAuthUser(userId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Reservation reservation = getReservation(reservationId, user, parkingZone);
            Reservation reviewedReservation = getReservation(reviewedReservationId, user, parkingZone); // 리뷰 작성된 예약

            ReservationWithReviewDto dto1 = new ReservationWithReviewDto(reservation, false);
            ReservationWithReviewDto dto2 = new ReservationWithReviewDto(reviewedReservation, true);

            PageRequest pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            List<ReservationWithReviewDto> content = List.of(dto1, dto2);
            Page<ReservationWithReviewDto> pageDto = new PageImpl<>(content, pageable, content.size());

            given(reservationReader.findMyReservations(anyLong(), any(PageRequest.class))).willReturn(pageDto);

            // when
            Page<MyReservationResponse> result = reservationService.getMyReservations(authUser, page, size);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten")
                    .contains(
                            tuple(reservationId, userId, parkingZoneId, parkingLot.getName(), false),
                            tuple(reviewedReservationId, userId, parkingZoneId, parkingLot.getName(), true)
                    );
        }

        @Test
        void 특정_사용자의_예약_리스트_조회_시_정상적이지_않은_page_기입_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            int page = -1;
            int size = 10;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;
            Long reviewedReservationId = 2L;

            AuthUser authUser = createAuthUser(userId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Reservation reservation = getReservation(reservationId, user, parkingZone);
            Reservation reviewedReservation = getReservation(reviewedReservationId, user, parkingZone); // 리뷰 작성된 예약

            ReservationWithReviewDto dto1 = new ReservationWithReviewDto(reservation, false);
            ReservationWithReviewDto dto2 = new ReservationWithReviewDto(reviewedReservation, true);

            PageRequest pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            List<ReservationWithReviewDto> content = List.of(dto1, dto2);
            Page<ReservationWithReviewDto> pageDto = new PageImpl<>(content, pageable, content.size());

            given(reservationReader.findMyReservations(anyLong(), any(PageRequest.class))).willReturn(pageDto);

            // when
            Page<MyReservationResponse> result = reservationService.getMyReservations(authUser, page, size);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten")
                    .contains(
                            tuple(reservationId, userId, parkingZoneId, parkingLot.getName(), false),
                            tuple(reviewedReservationId, userId, parkingZoneId, parkingLot.getName(), true)
                    );
        }

        @Nested
        class GetReservationById {

            @Test
            void 특정_사용자의_특정_예약_단건_조회_테스트() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);

                User owner = createOwner(ownerId);
                User user = createUser(authUser.getId());

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(reservationId, user, parkingZone);

                boolean isReviewWritten = false;

                given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);
                given(reviewReader.isReviewWritten(anyLong())).willReturn(isReviewWritten);

                // when
                MyReservationResponse result = reservationService.getMyReservation(authUser, reservationId);

                // then
                assertThat(result)
                        .isNotNull()
                        .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten")
                        .isEqualTo(
                                List.of(reservationId, userId, parkingZoneId, parkingLot.getName(), false)
                        );
            }
        }

        @Nested
        class GetReservationsByParkingZoneId {

            @Test
            void 특정_주차공간에_대한_예약_내역_리스트_조회_테스트() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                int page = 1;
                int size = 10;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;
                Long reservationId = 1L;

                AuthUser authOwner = createAuthOwner(ownerId);

                User owner = createOwner(authOwner.getId());
                User user = createUser(userId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(reservationId, user, parkingZone);

                Page<Reservation> pageMyReservations = new PageImpl<>(List.of(reservation));

                given(parkingZoneReader.existsById(anyLong())).willReturn(true);
                given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);
                given(reservationReader.findOwnerReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);

                // when
                Page<OwnerReservationResponse> result = reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size);

                // then
                assertThat(result.getContent())
                        .isNotNull()
                        .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName")
                        .contains(
                                tuple(reservationId, userId, parkingZoneId, parkingLot.getName())
                        );
            }

            @Test
            void 특정_주차공간에_대한_예약_내역_조회_시_정상적이지_않은_page_기입_테스트() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                int page = -1;
                int size = 10;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;
                Long reservationId = 1L;

                AuthUser authOwner = createAuthOwner(ownerId);

                User owner = createOwner(authOwner.getId());
                User user = createUser(userId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(reservationId, user, parkingZone);

                Page<Reservation> pageMyReservations = new PageImpl<>(List.of(reservation));

                given(parkingZoneReader.existsById(anyLong())).willReturn(true);
                given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);
                given(reservationReader.findOwnerReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);

                // when
                Page<OwnerReservationResponse> result = reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size);

                // then
                assertThat(result.getContent())
                        .isNotNull()
                        .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName")
                        .contains(
                                tuple(reservationId, userId, parkingZoneId, parkingLot.getName())
                        );
            }

            @Test
            void 특정_주차공간에_대한_예약_내역_조회_시_주차공간이_없을_경우_NOT_FOUND_PARKING_ZONE_예외_처리() {
                // given
                Long ownerId = 1L;
                Long parkingZoneId = -1L;
                int page = 1;
                int size = 10;

                AuthUser authOwner = createAuthOwner(ownerId);

                given(parkingZoneReader.existsById(anyLong())).willReturn(false);

                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size));
                assertEquals(ReservationErrorCode.NOT_FOUND_PARKING_ZONE, exception.getErrorCode());
            }

            @Test
            void 특정_주차공간에_대한_예약_내역_조회_시_본인_주차공간이_아닐_경우_NOT_MY_PARKING_ZONE_예외_처리() {
                // given
                Long ownerId = 1L;
                Long differentUserId = 2L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;
                int page = 1;
                int size = 10;

                AuthUser authOwner = createAuthOwner(ownerId);

                User differentOwner = createOwner(differentUserId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, differentOwner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                given(parkingZoneReader.existsById(anyLong())).willReturn(true);
                given(parkingZoneReader.findById(anyLong())).willReturn(parkingZone);

                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size));
                assertEquals(ReservationErrorCode.NOT_MY_PARKING_ZONE, exception.getErrorCode());
            }
        }

        @Nested
        class CompleteReservation {

            @Test
            void CONFIRMED_상태의_특정_예약_사용_완료_테스트() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);

                given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);
                doNothing().when(reservationWriter).complete(reservation);

                // when
                reservationService.completeReservation(authUser, reservationId);

                // then
                verify(reservationWriter, times(1)).complete(reservation);
            }

            @Test
            void 예약_사용_완료_시_예약의_상태가_CONFIRMED가_아닐_경우_CANT_MODIFY_RESERVATION_STATUS_예외_처리() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.PENDING);

                given(reservationReader.findReservation(userId, reservationId)).willReturn(reservation);

                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.completeReservation(authUser, reservationId));
                assertEquals(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS, exception.getErrorCode());
            }
        }

        @Nested
        class CancelReservation {

            @Test
            void CONFIRMED_상태의_특정_예약_취소_테스트() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                LocalDateTime startDateTime = LocalDateTime.now().plusHours(3);
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);
                ReflectionTestUtils.setField(reservation, "startDateTime", startDateTime);

                given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
                doNothing().when(reservationWriter).cancel(reservation);

                // when
                reservationService.cancelReservation(authUser, reservationId);

                // then
                verify(reservationWriter, times(1)).cancel(reservation);
            }

            @Test
            void 예약_취소_시_예약의_상태가_COMPLETED_일_경우_CANT_CANCEL_COMPLETED_RESERVATION_예외_처리() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.COMPLETED);

                given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.cancelReservation(authUser, reservationId));
                assertEquals(ReservationErrorCode.CANT_CANCEL_COMPLETED_RESERVATION, exception.getErrorCode());
            }

            @Test
            void 예약_취소_시_예약의_상태가_CANCELED_일_경우_CANT_CANCEL_CANCELED_RESERVATION_예외_처리() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CANCELED);

                given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);
                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.cancelReservation(authUser, reservationId));
                assertEquals(ReservationErrorCode.CANT_CANCEL_CANCELED_RESERVATION, exception.getErrorCode());
            }

            @Test
            void 예약_취소_시_예약시간까지_남은_시간이_1시간_이내일_경우_CANT_CANCEL_WITHIN_ONE_HOUR_예외_처리() {
                // given
                Long ownerId = 1L;
                Long userId = 2L;
                Long reservationId = 1L;
                LocalDateTime startDateTime = LocalDateTime.now();
                Long parkingLotId = 1L;
                Long parkingZoneId = 1L;

                AuthUser authUser = createAuthUser(userId);
                User user = createUser(authUser.getId());
                User owner = createOwner(ownerId);

                ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

                ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

                Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
                ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);
                ReflectionTestUtils.setField(reservation, "startDateTime", startDateTime);

                given(reservationReader.findReservation(anyLong(), any(Long.class))).willReturn(reservation);

                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> reservationService.cancelReservation(authUser, reservationId));
                assertEquals(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR, exception.getErrorCode());
            }
        }
    }
}