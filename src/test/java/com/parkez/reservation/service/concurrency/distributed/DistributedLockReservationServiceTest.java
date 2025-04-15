package com.parkez.reservation.service.concurrency.distributed;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.reservation.service.ReservationWriter;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
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
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DistributedLockReservationServiceTest {

    @Mock
    private DistributedLockManager distributedLockManager;
    @Mock
    private UserReader userReader;
    @Mock
    private ParkingZoneReader parkingZoneReader;
    @Mock
    private ReservationWriter reservationWriter;
    @Mock
    private ReservationReader reservationReader;
    @InjectMocks
    private DistributedLockReservationService distributedLockReservationService;

    private static AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.USER)
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
                .parkingLotName(parkingZone.getParkingLotName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .price(price)
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

            BigDecimal price = BigDecimal.valueOf(2000);
            Reservation reservation = createReservation(reservationId, user, parkingZone, request, price);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<MyReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(reservationWriter.create(any(User.class), any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(reservation);

            // when
            MyReservationResponse result = distributedLockReservationService.createReservation(authUser, request);

            // then
            assertThat(result)
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten", "startDateTime", "endDateTime", "price")
                    .isEqualTo(
                            List.of(reservationId, userId, parkingZoneId, parkingLot.getName(), false, request.getStartDateTime(), request.getEndDateTime(), price)
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_잘못된_예약날짜_및_시간_입력의_경우_NOT_VALID_REQUEST_TIME_예외_처리() {
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

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<MyReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> distributedLockReservationService.createReservation(authUser, request));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_이미_해당_시간에_예약이_존재할_경우_ALREADY_RESERVED_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId);
            ReflectionTestUtils.setField(request, "endDateTime", request.getStartDateTime().plusHours(1));

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<MyReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(reservationReader.existsReservationByConditions(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList())).willReturn(true);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> distributedLockReservationService.createReservation(authUser, request));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.ALREADY_RESERVED);
        }
    }
}