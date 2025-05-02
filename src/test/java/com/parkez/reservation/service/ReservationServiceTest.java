package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.payment.service.PaymentService;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.Promotion;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.domain.enums.DiscountType;
import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.domain.enums.PromotionType;
import com.parkez.promotion.service.PromotionIssueReader;
import com.parkez.promotion.service.PromotionIssueValidator;
import com.parkez.promotion.service.PromotionIssueWriter;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.dto.WaitingUserDto;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.queue.service.QueueService;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Callable;

import static com.parkez.promotion.excption.PromotionIssueErrorCode.*;
import static org.assertj.core.api.Assertions.*;
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
    @Mock
    private DistributedLockManager distributedLockManager;

    @Mock
    private PromotionIssueReader promotionIssueReader;

    @Mock
    private PromotionIssueValidator promotionIssueValidator;

    @Mock
    private PromotionIssueWriter promotionIssueWriter;

    @Mock
    private PaymentService paymentService;

    @Mock
    private QueueService queueService;

    @InjectMocks
    private ReservationService reservationService;

    private static final LocalTime OPENED_AT = LocalTime.of(9, 0, 0);
    private static final LocalTime CLOSED_AT = LocalTime.of(21, 0, 0);
    private static final LocalDateTime RESERVATION_START_DATE_TIME = LocalDateTime.of(LocalDate.now().plusDays(1), OPENED_AT);
    private static final LocalDateTime RESERVATION_END_DATE_TIME = LocalDateTime.of(LocalDate.now().plusDays(1), CLOSED_AT);

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
                .openedAt(OPENED_AT)
                .closedAt(CLOSED_AT)
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", id);
        return parkingLot;
    }

    private static ParkingZone createParkingZone(Long id, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", id);
        ReflectionTestUtils.setField(parkingZone, "status", ParkingZoneStatus.AVAILABLE);
        return parkingZone;
    }

    private static Reservation createReservation(Long id, User user, ParkingZone parkingZone, ReservationRequest request, BigDecimal price,
        BigDecimal originalPrice, BigDecimal discountAmount, Long promotionIssueId) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLotName())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .price(price)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .promotionIssueId(promotionIssueId)
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private static Reservation getReservation(Long id, User user, ParkingZone parkingZone) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLotName())
                .build();
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private static ReservationRequest createRequest(Long id, Long promotionIssueId) {
        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", id);
        ReflectionTestUtils.setField(request, "startDateTime", RESERVATION_START_DATE_TIME);
        ReflectionTestUtils.setField(request, "endDateTime", RESERVATION_END_DATE_TIME);
        ReflectionTestUtils.setField(request, "promotionIssueId", promotionIssueId);
        return request;
    }

    private PromotionIssue createPromotionIssue(Promotion promotion, AuthUser authUser, LocalDateTime issuedAt,
        LocalDateTime expiresAt) {
        return PromotionIssue.builder()
            .promotion(promotion)
            .user(User.from(authUser))
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();
    }

    private Promotion createPromotion(Long promotionId, String promotionName, PromotionType promotionType,
        Coupon coupon, int limitTotal,
        int limitPerUser, LocalDateTime promotionStartAt, LocalDateTime promotionEndAt, int validDaysAfterIssue,
        PromotionStatus promotionStatus) {
        Promotion promotion = Promotion.builder()
            .name(promotionName)
            .promotionType(promotionType)
            .coupon(coupon)
            .limitTotal(limitTotal)
            .limitPerUser(limitPerUser)
            .promotionStartAt(promotionStartAt)
            .promotionEndAt(promotionEndAt)
            .validDaysAfterIssue(validDaysAfterIssue)
            .build();
        ReflectionTestUtils.setField(promotion, "id", promotionId);
        ReflectionTestUtils.setField(promotion, "promotionStatus", promotionStatus);
        return promotion;
    }

    private Coupon createCoupon(Long id, String name, DiscountType discountType, int discountValue,
        String description) {
        Coupon coupon = Coupon.builder()
            .name(name)
            .discountType(discountType)
            .discountValue(discountValue)
            .description(description)
            .build();
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }

    private static ReservationRequest createReservationRequest() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        return new ReservationRequest(1L, start, end);
    }

    @Nested
    class CreateReservation {

        @Test
        void 특정_주차공간에_존재하지않는_쿠폰으로_예약하면_PROMOTION_ISSUE_NOT_FOUND_예외_발생한다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            Long promotionIssueId = -1L;
            ReservationRequest request = createRequest(parkingZoneId, promotionIssueId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(promotionIssueReader.getWithPromotionAndCouponById(anyLong())).willThrow(new ParkingEasyException(PROMOTION_ISSUE_NOT_FOUND));

            // when & then
            assertThatThrownBy(()->reservationService.createReservation(authUser, request, LocalDateTime.now()))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessage(PROMOTION_ISSUE_NOT_FOUND.getDefaultMessage());

        }

        @Test
        void 만료된_쿠폰으로_예약하면_EXPIRED_COUPON_예외가_발생한다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            Long promotionIssueId = 1L;
            ReservationRequest request = createRequest(parkingZoneId, promotionIssueId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Long promotionId = 1L;
            String promotionName = "DAILY 2000";
            PromotionType promotionType = PromotionType.DAILY;
            int limitTotal = 100;
            int limitPerUser = 1;
            int validDaysAfterIssue = 3;

            LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
            LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

            long couponId = 1L;
            String couponName = "신규가입 2000원 할인 쿠폰";
            DiscountType discountType = DiscountType.FIXED;
            int discountValue = 2000;
            String description = "신규 유저 전용, 1회만 사용 가능";

            Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
            Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
            PromotionIssue promotionIssue = createPromotionIssue(promotion, authUser, issuedAt, expiresAt);


            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(promotionIssueReader.getWithPromotionAndCouponById(anyLong())).willReturn(promotionIssue);
            doThrow(new ParkingEasyException(EXPIRED_COUPON)).when(promotionIssueValidator).validateCanBeUsed(any(),any());

            // when & then
            assertThatThrownBy(()->reservationService.createReservation(authUser, request, LocalDateTime.now()))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessage(EXPIRED_COUPON.getDefaultMessage());

        }

        @Test
        void 이미_사용한_쿠폰으로_예약하면_ALREADY_USED_예외가_발생한다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            Long promotionIssueId = 1L;
            ReservationRequest request = createRequest(parkingZoneId, promotionIssueId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Long promotionId = 1L;
            String promotionName = "DAILY 2000";
            PromotionType promotionType = PromotionType.DAILY;
            int limitTotal = 100;
            int limitPerUser = 1;
            int validDaysAfterIssue = 3;

            LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
            LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

            long couponId = 1L;
            String couponName = "신규가입 2000원 할인 쿠폰";
            DiscountType discountType = DiscountType.FIXED;
            int discountValue = 2000;
            String description = "신규 유저 전용, 1회만 사용 가능";

            Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
            Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
            PromotionIssue promotionIssue = createPromotionIssue(promotion, authUser, issuedAt, expiresAt);


            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(promotionIssueReader.getWithPromotionAndCouponById(anyLong())).willReturn(promotionIssue);
            doThrow(new ParkingEasyException(ALREADY_USED)).when(promotionIssueValidator).validateCanBeUsed(any(),any());

            // when & then
            assertThatThrownBy(()->reservationService.createReservation(authUser, request, LocalDateTime.now()))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessage(ALREADY_USED.getDefaultMessage());

        }

        @Test
        void 다른_사람의_쿠폰으로_예약하면_NOT_YOUR_COUPON_예외가_발생한다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);
            AuthUser anotherAuthUser = createAuthUser(3L);

            Long promotionIssueId = 1L;
            ReservationRequest request = createRequest(parkingZoneId, promotionIssueId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Long promotionId = 1L;
            String promotionName = "DAILY 2000";
            PromotionType promotionType = PromotionType.DAILY;
            int limitTotal = 100;
            int limitPerUser = 1;
            int validDaysAfterIssue = 3;

            LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
            LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

            long couponId = 1L;
            String couponName = "신규가입 2000원 할인 쿠폰";
            DiscountType discountType = DiscountType.FIXED;
            int discountValue = 2000;
            String description = "신규 유저 전용, 1회만 사용 가능";

            Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
            Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
            PromotionIssue promotionIssue = createPromotionIssue(promotion, anotherAuthUser, issuedAt, expiresAt);


            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(promotionIssueReader.getWithPromotionAndCouponById(anyLong())).willReturn(promotionIssue);

            // when & then
            assertThatThrownBy(()->reservationService.createReservation(authUser, request, LocalDateTime.now()))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessage(NOT_YOUR_COUPON.getDefaultMessage());

        }

        @Test
        void 특정_주차공간에_쿠폰을_적용해서_예약_생성_할_수_있다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;

            AuthUser authUser = createAuthUser(userId);

            Long promotionIssueId = 1L;
            ReservationRequest request = createRequest(parkingZoneId, promotionIssueId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Long promotionId = 1L;
            String promotionName = "DAILY 2000";
            PromotionType promotionType = PromotionType.DAILY;
            int limitTotal = 100;
            int limitPerUser = 1;
            int validDaysAfterIssue = 3;

            LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
            LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

            long couponId = 1L;
            String couponName = "신규가입 2000원 할인 쿠폰";
            DiscountType discountType = DiscountType.FIXED;
            int discountValue = 2000;
            String description = "신규 유저 전용, 1회만 사용 가능";



            Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
            Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
            PromotionIssue promotionIssue = createPromotionIssue(promotion, authUser, issuedAt, expiresAt);

            long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
            BigDecimal originalPrice = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
            BigDecimal discountAmount = coupon.calculateDiscount(originalPrice);
            BigDecimal finalPrice = originalPrice.subtract(discountAmount);

            Reservation reservation = createReservation(reservationId, user, parkingZone, request, finalPrice, originalPrice,
                discountAmount, promotionIssueId);


            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(promotionIssueReader.getWithPromotionAndCouponById(anyLong())).willReturn(promotionIssue);
            given(reservationWriter.create(any(User.class), any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class),any(BigDecimal.class),any(
                BigDecimal.class),any(BigDecimal.class),any()))
                .willReturn(reservation);

            // when
            ReservationResponse result = reservationService.createReservation(authUser, request, LocalDateTime.now());

            // then
            assertThat(result)
                .isNotNull()
                .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten", "startDateTime", "endDateTime", "price", "originalPrice", "discountAmount")
                .isEqualTo(
                    List.of(reservationId, userId, parkingZoneId, parkingLot.getName(), false, request.getStartDateTime(), request.getEndDateTime(), finalPrice,originalPrice, discountAmount)
                );
        }

        @Test
        void 특정_주차공간에_쿠폰을_사용하지않고_예약_생성_할_수_있다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
            BigDecimal originalPrice = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal price = originalPrice.subtract(discountAmount);
            Long  promotionIssueId = null;

            Reservation reservation = createReservation(reservationId, user, parkingZone, request, price, originalPrice,
                discountAmount, promotionIssueId);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(reservationWriter.create(any(User.class), any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class),any(BigDecimal.class),any(
                BigDecimal.class),any(BigDecimal.class),any()))
                    .willReturn(reservation);

            // when
            ReservationResponse result = reservationService.createReservation(authUser, request, LocalDateTime.now());

            // then
            assertThat(result)
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten", "startDateTime", "endDateTime", "price")
                    .isEqualTo(
                            List.of(reservationId, userId, parkingZoneId, parkingLot.getName(), false, request.getStartDateTime(), request.getEndDateTime(), price)
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_시작_시간이_종료_시간보다_늦을_경우_NOT_VALID_REQUEST_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);
            ReflectionTestUtils.setField(request, "endDateTime", request.getStartDateTime().minusNanos(1));

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_과거_시간에_예약을_하는_경우_NOT_VALID_REQUEST_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            LocalDateTime startDateTime = LocalDateTime.now().minusNanos(1);

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);
            ReflectionTestUtils.setField(request, "startDateTime", startDateTime);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_예약_기간이_하루를_초과하여_예약을_하는_경우_NOT_VALID_REQUEST_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            LocalDateTime endDateTime = RESERVATION_END_DATE_TIME.plusDays(1);

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);
            ReflectionTestUtils.setField(request, "endDateTime", endDateTime);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_parkingZone_의_상태가_UNAVAILABLE_일_경우_CANT_RESERVE_UNAVAILABLE_PARKING_ZONE_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);
            ReflectionTestUtils.setField(parkingZone, "status", ParkingZoneStatus.UNAVAILABLE);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_RESERVE_UNAVAILABLE_PARKING_ZONE);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_예약_시작_시간이_OPEND_AT_이전일_경우_CANT_RESERVE_AT_CLOSE_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            LocalDateTime startDateTime = RESERVATION_START_DATE_TIME.minusNanos(1);

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);
            ReflectionTestUtils.setField(request, "startDateTime", startDateTime);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_RESERVE_AT_CLOSE_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_예약_종료_시간이_CLOSED_AT_이후일_경우_CANT_RESERVE_AT_CLOSE_TIME_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            LocalDateTime endDateTime = RESERVATION_END_DATE_TIME.plusNanos(1);

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);
            ReflectionTestUtils.setField(request, "endDateTime", endDateTime);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_RESERVE_AT_CLOSE_TIME);
        }

        @Test
        void 특정_주차공간에_대한_예약_생성_시_이미_해당_시간에_예약이_존재하면_대기열_등록되고_null_반환() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);
            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(anyLong())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(reservationReader.existsReservationByConditions(any(ParkingZone.class), any(LocalDateTime.class), any(LocalDateTime.class), anyList())).willReturn(true);
            given(queueService.joinWaitingQueue(anyLong(), any())).willReturn(JoinQueueResult.JOINED);

            // when
            ReservationResponse response = reservationService.createReservation(authUser, request, LocalDateTime.now());

            // then
            assertThat(response).isNull();
        }

        @Test
        void 락_선점_실패시_대기열_등록하고_null을_반환한다() {
            // given
            Long userId = 1L;
            Long parkingZoneId = 1L;
            AuthUser authUser = createAuthUser(userId);
            ReservationRequest request = createRequest(parkingZoneId, null);

            // 락 선점 실패 상황을 가짜로 만들어줌
            given(distributedLockManager.executeWithLock(anyLong(), any()))
                    .willThrow(new ParkingEasyException(ReservationErrorCode.RESERVATION_LOCK_FAILED));
            given(userReader.getActiveUserById(anyLong())).willReturn(createUser(userId));
            given(queueService.joinWaitingQueue(anyLong(), any())).willReturn(JoinQueueResult.JOINED);

            // when
            ReservationResponse result = reservationService.createReservation(authUser, request, LocalDateTime.now());

            // then
            assertThat(result).isNull();
        }



        @Test
        void 올바른_예약_요청_시간에_대한_검증_테스트() {
            // given
            Long parkingZoneId = 1L;
            ReservationRequest request = createRequest(parkingZoneId, null);

            // when
            boolean result = reservationService.validateRequestTime(request);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 본인이_이미_예약한_경우_예외발생() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;

            AuthUser authUser = createAuthUser(userId);

            ReservationRequest request = createRequest(parkingZoneId, null);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);
            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

            given(distributedLockManager.executeWithLock(anyLong(), any())).willAnswer(invocation -> {
                Callable<ReservationResponse> task = invocation.getArgument(1);
                return task.call();
            });
            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(parkingZoneId)).willReturn(parkingZone);
            given(reservationReader.existsReservationByConditionsForUser(parkingZone, request.getStartDateTime(), request.getEndDateTime(), userId, statusList))
                    .willReturn(true);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.createReservation(authUser, request, LocalDateTime.now()));

            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.ALREADY_RESERVED_BY_YOURSELF);
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
            Page<ReservationResponse> result = reservationService.getMyReservations(authUser, page, size);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName", "reviewWritten")
                    .contains(
                            tuple(reservationId, userId, parkingZoneId, parkingLot.getName(), false),
                            tuple(reviewedReservationId, userId, parkingZoneId, parkingLot.getName(), true)
                    );
        }
    }

    @Nested
    class GetReservationById {

        @Test
        void 특정_사용자의_특정_예약_조회_테스트() {
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

            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(reviewReader.isReviewWritten(anyLong())).willReturn(isReviewWritten);

            // when
            ReservationResponse result = reservationService.getMyReservation(authUser, reservationId);

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

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);
            given(reservationReader.findOwnerReservations(anyLong(), any(PageRequest.class))).willReturn(pageMyReservations);

            // when
            Page<ReservationResponse> result = reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size);

            // then
            assertThat(result.getContent())
                    .isNotNull()
                    .extracting("reservationId", "userId", "parkingZoneId", "parkingLotName")
                    .contains(
                            tuple(reservationId, userId, parkingZoneId, parkingLot.getName())
                    );
        }

        @Test
        void 특정_주차공간에_대한_예약_내역_조회_시_주차공간이_없을_경우_PARKING_ZONE_NOT_FOUND_예외_처리() {
            // given
            Long ownerId = 1L;
            Long parkingZoneId = -1L;
            int page = 1;
            int size = 10;

            AuthUser authOwner = createAuthOwner(ownerId);

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willThrow(new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size));
            assertThat(exception.getErrorCode()).isEqualTo(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
        }

        @Test
        void 특정_주차공간에_대한_예약_내역_조회_시_본인_소유의_주차공간이_아닐_경우_NOT_PARKING_LOT_OWNER_예외_처리() {
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

            given(parkingZoneReader.getActiveByParkingZoneId(anyLong())).willReturn(parkingZone);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.getOwnerReservations(authOwner, parkingZoneId, page, size));
            assertThat(exception.getErrorCode()).isEqualTo(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
        }
    }

    @Nested
    class CompleteReservation {

        @Test
        void 특정_예약_사용_완료_시_CONFIRMED_상태의_예약_사용_완료_테스트() {
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

            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            doNothing().when(reservationWriter).complete(reservation);

            // when
            reservationService.completeReservation(authUser, reservationId);

            // then
            verify(reservationWriter, times(1)).complete(reservation);
        }

        @Test
        void 특정_예약_사용_완료_시_예약의_상태가_CONFIRMED가_아닐_경우_CANT_MODIFY_RESERVATION_STATUS_예외_처리() {
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

            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.completeReservation(authUser, reservationId));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_MODIFY_RESERVATION_STATUS);
        }
    }

    @Nested
    class CancelReservation {

        @Test
        void 예약_취소시_사용했던_쿠폰이_만료되지_않았으면_복구한다() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long reservationId = 1L;
            LocalDateTime startDateTime = LocalDateTime.now().plusHours(3);
            LocalDateTime endDateTime = LocalDateTime.now().plusHours(4);
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
            ReflectionTestUtils.setField(reservation, "endDateTime", endDateTime);
            ReflectionTestUtils.setField(reservation, "promotionIssueId", 1L);

            Long promotionId = 1L;
            String promotionName = "DAILY 2000";
            PromotionType promotionType = PromotionType.DAILY;
            int limitTotal = 100;
            int limitPerUser = 1;
            int validDaysAfterIssue = 3;

            LocalDateTime promotionStartAt = LocalDateTime.now().plusDays(1);
            LocalDateTime promotionEndAt = LocalDateTime.now().plusDays(2);

            long couponId = 1L;
            String couponName = "신규가입 2000원 할인 쿠폰";
            DiscountType discountType = DiscountType.FIXED;
            int discountValue = 2000;
            String description = "신규 유저 전용, 1회만 사용 가능";

            Coupon coupon = createCoupon(couponId,couponName,discountType,discountValue,description);
            Promotion promotion = createPromotion(promotionId,promotionName,promotionType,coupon,limitTotal,limitPerUser,promotionStartAt,promotionEndAt,validDaysAfterIssue,PromotionStatus.ACTIVE);

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusDays(promotion.getValidDaysAfterIssue());
            PromotionIssue promotionIssue = createPromotionIssue(promotion, authUser, issuedAt, expiresAt);

            given(reservationReader.findMyReservation(anyLong(), any(Long.class))).willReturn(reservation);
            given(promotionIssueReader.getById(anyLong())).willReturn(promotionIssue);
            doNothing().when(reservationWriter).cancel(reservation);
            doNothing().when(promotionIssueWriter).cancelUsage(promotionIssue);

            // when
            ReservationCancelRequest request = new ReservationCancelRequest();
            reservationService.cancelReservation(authUser, reservationId, request, LocalDateTime.now());

            // then
            verify(promotionIssueWriter).cancelUsage(promotionIssue);
        }

        @Test
        void 특정_예약_취소_시_CONFIRMED_상태의_특정_예약_취소_테스트() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long reservationId = 1L;
            LocalDateTime startDateTime = LocalDateTime.now().plusHours(3);
            LocalDateTime endDateTime = LocalDateTime.now().plusHours(4);
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
            ReflectionTestUtils.setField(reservation, "endDateTime", endDateTime);

            given(reservationReader.findMyReservation(anyLong(), any(Long.class))).willReturn(reservation);
            doNothing().when(reservationWriter).cancel(reservation);

            // when
            ReservationCancelRequest request = new ReservationCancelRequest();
            reservationService.cancelReservation(authUser, reservationId, request, LocalDateTime.now());

            // then
            verify(reservationWriter, times(1)).cancel(reservation);
        }

        @Test
        void 특정_예약_취소_시_예약의_상태가_PENDING_및_CONFIRMED가_아닐_경우_CANT_CANCEL_RESERVATION_예외_처리() {
            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long reservationId = 1L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            ReservationStatus status = ReservationStatus.COMPLETED;

            AuthUser authUser = createAuthUser(userId);
            User user = createUser(authUser.getId());
            User owner = createOwner(ownerId);

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            Reservation reservation = getReservation(parkingZoneId, user, parkingZone);
            ReflectionTestUtils.setField(reservation, "status", status);

            given(reservationReader.findMyReservation(anyLong(), any(Long.class))).willReturn(reservation);
            // when & then
            ReservationCancelRequest request = new ReservationCancelRequest();

            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.cancelReservation(authUser, reservationId, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_CANCEL_RESERVATION);
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

            given(reservationReader.findMyReservation(anyLong(), any(Long.class))).willReturn(reservation);

            // when & then
            ReservationCancelRequest request = new ReservationCancelRequest();

            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> reservationService.cancelReservation(authUser, reservationId, request, LocalDateTime.now()));
            assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.CANT_CANCEL_WITHIN_ONE_HOUR);
        }
    }

    @Nested
    class ExpireReservation {

        @Test
        void 예약_생성_후_10분_이내_결제_요청_생성하지_않을_경우_예약_만료() {
            // given
            Reservation expiredReservation = mock(Reservation.class);
            given(reservationWriter.expire(any(LocalDateTime.class)))
                    .willReturn(List.of(expiredReservation)); // << 여기! List 반환!

            given(expiredReservation.getParkingZoneId()).willReturn(1L);
            given(expiredReservation.getStartDateTime()).willReturn(LocalDateTime.now().plusHours(1));
            given(expiredReservation.getEndDateTime()).willReturn(LocalDateTime.now().plusHours(2));
            given(queueService.dequeueConvertToDto(anyString())).willReturn(null); // 대기열 비어있게

            // when
            reservationService.expireReservation();

            // then
            verify(reservationWriter, times(1)).expire(any(LocalDateTime.class));
        }
    }

    @Nested
    class HandleQueueOnLockFailTest {

        @Test
        void 락_선점실패시_정상적으로_대기열_등록() {
            // given
            Long userId = 1L;
            AuthUser authUser = createAuthUser(1L);

            ReservationRequest request = createReservationRequest();
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(queueService.joinWaitingQueue(userId, request)).willReturn(JoinQueueResult.JOINED);

            // when & then
            assertThatCode(() ->
                    ReflectionTestUtils.invokeMethod(reservationService, "handleQueueOnLockFail", authUser, request)
            ).doesNotThrowAnyException();
        }

        @Test
        void 락_선점실패시_이미_대기열_등록되어있으면_ALREADY_IN_QUEUE_예외_발생() {
            // given
            Long userId = 1L;
            AuthUser authUser = createAuthUser(1L);

            ReservationRequest request = createReservationRequest();
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(queueService.joinWaitingQueue(userId, request)).willReturn(JoinQueueResult.ALREADY_JOINED);

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(reservationService, "handleQueueOnLockFail", authUser, request))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining("이미 대기열에 등록된 사용자입니다.");
        }
    }

    @Nested
    class HandleJoinQueueTest {

        @Test
        void 예약_중복시_정상적으로_대기열_등록() {
            // given
            Long userId = 1L;

            ReservationRequest request = createReservationRequest();
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            given(queueService.joinWaitingQueue(userId, request)).willReturn(JoinQueueResult.JOINED);

            // when & then
            assertThatCode(() ->
                    ReflectionTestUtils.invokeMethod(reservationService, "handleJoinQueue", user, request)
            ).doesNotThrowAnyException();
        }

        @Test
        void 예약_중복시_이미_대기열_등록되어있으면_ALREADY_IN_QUEUE_예외_발생() {
            // given
            Long userId = 1L;

            ReservationRequest request = createReservationRequest();
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            given(queueService.joinWaitingQueue(userId, request)).willReturn(JoinQueueResult.ALREADY_JOINED);

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(reservationService, "handleJoinQueue", user, request))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining("이미 대기열에 등록된 사용자입니다.");
        }
    }

    @Nested
    class HandleNextInQueueTest {

        @Test
        void 대기열에_사용자없으면_아무일도_일어나지_않는다() {
            // given
            Reservation reservation = mock(Reservation.class);

            given(reservation.getParkingZoneId()).willReturn(1L);
            given(reservation.getStartDateTime()).willReturn(LocalDateTime.now().plusHours(1));
            given(reservation.getEndDateTime()).willReturn(LocalDateTime.now().plusHours(2));
            given(queueService.dequeueConvertToDto(anyString())).willReturn(null);

            // when & then
            assertThatCode(() ->
                    ReflectionTestUtils.invokeMethod(reservationService, "handleNextInQueue", reservation)
            ).doesNotThrowAnyException();

            verify(userReader, never()).getActiveUserById(any());
        }

        @Test
        void 대기열에_사용자있으면_예약생성된다() {
            // given
            Reservation reservation = mock(Reservation.class);

            WaitingUserDto waitingUserDto = new WaitingUserDto(
                    1L,
                    1L,
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(2)
            );

            User user = mock(User.class);
            ParkingZone parkingZone = mock(ParkingZone.class);

            given(reservation.getParkingZoneId()).willReturn(1L);
            given(reservation.getStartDateTime()).willReturn(waitingUserDto.getReservationStartDateTime());
            given(reservation.getEndDateTime()).willReturn(waitingUserDto.getReservationEndDateTime());

            given(queueService.dequeueConvertToDto(anyString())).willReturn(waitingUserDto);
            given(userReader.getActiveUserById(waitingUserDto.getUserId())).willReturn(user);
            given(parkingZoneReader.getActiveByParkingZoneId(waitingUserDto.getParkingZoneId())).willReturn(parkingZone);
            given(parkingZone.getParkingLotPricePerHour()).willReturn(BigDecimal.valueOf(1000));

            // when
            ReflectionTestUtils.invokeMethod(reservationService, "handleNextInQueue", reservation);

            // then
            verify(userReader).getActiveUserById(waitingUserDto.getUserId());
            verify(parkingZoneReader).getActiveByParkingZoneId(waitingUserDto.getParkingZoneId());
            verify(reservationWriter).create(
                    eq(user),
                    eq(parkingZone),
                    eq(waitingUserDto.getReservationStartDateTime()),
                    eq(waitingUserDto.getReservationEndDateTime()),
                    any(BigDecimal.class),
                    eq(BigDecimal.ZERO),
                    any(BigDecimal.class),
                    isNull()
            );
        }

    }


}
