package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.domain.enums.PaymentType;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.request.PaymentFailRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.dto.response.PaymentResponse;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import com.parkez.reservation.dto.request.ReservationRequest;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentWriter paymentWriter;

    @Mock
    private PaymentReader paymentReader;

    @Mock
    private UserReader userReader;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private ReservationWriter reservationWriter;

    @Mock
    private TossPaymentService tossPaymentService;

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
        ReflectionTestUtils.setField(reservation, "createdAt", LocalDateTime.now().minusMinutes(1));
        return reservation;
    }

    private PaymentCreateRequest getPaymentCreateRequest(Long id) {
        PaymentCreateRequest request = new PaymentCreateRequest();
        ReflectionTestUtils.setField(request, "reservationId", id);
        return request;
    }

    private static ReservationRequest createRequest(Long id) {
        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = start.plusHours(1);

        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", id);
        ReflectionTestUtils.setField(request, "startDateTime", start);
        ReflectionTestUtils.setField(request, "endDateTime", end);

        return request;
    }

    private Payment createPayment(Long id, User user, Reservation reservation, String orderId) {
        Payment payment = Payment.builder()
                .user(user)
                .reservation(reservation)
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .paymentKey("")
                .cardFee(0)
                .build();
        ReflectionTestUtils.setField(payment, "id", id);

        return payment;
    }

    @Nested
    class createPayment {

        @Test
        void 특정_예약에_대하여_정상적으로_결제_요청_생성(){

            // given
            Long ownerId = 1L;
            Long userId = 2L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            Long reservationId = 1L;
            Long paymentId = 1L;
            String orderId = "test-order-id";

            AuthUser authUser = createAuthUser(userId);

            User owner = createOwner(ownerId);
            User user = createUser(authUser.getId());

            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            ReservationRequest reservationRequest = createRequest(parkingZoneId);

            long hours = ChronoUnit.HOURS.between(reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime());
            BigDecimal price = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
            Reservation reservation = createReservation(reservationId, user, parkingZone, reservationRequest, price);

            PaymentCreateRequest paymentCreateRequest = getPaymentCreateRequest(reservationId);

            Payment payment = createPayment(paymentId, user, reservation, orderId);

            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(paymentWriter.createPayment(eq(user), eq(reservation), eq(orderId))).willReturn(payment);

            //when
            PaymentCreateResponse paymentCreateResponse = paymentService.createPayment(authUser, paymentCreateRequest, orderId);

            //then
            assertThat(paymentCreateResponse).isNotNull()
                    .extracting("paymentId", "price", "orderId")
                    .containsExactly(1L, price, orderId);

        }
    }

    @Nested
    class checkReservationTimeout{
        @Test
        void 예약_후_결제_생성할_때_시간초과시_PAYMENT_TIME_OUT_예외발생(){
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            String orderId = "test-order-id";

            AuthUser authUser = createAuthUser(userId);
            User user = createUser(userId);
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.PENDING);
            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(reservation.isTimeout(any(), anyLong())).willReturn(true);

            PaymentCreateRequest request = getPaymentCreateRequest(reservationId);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> paymentService.createPayment(authUser, request, orderId));

            assertEquals(PaymentErrorCode.PAYMENT_TIME_OUT, exception.getErrorCode());
        }
    }

    @Nested
    class validatePaymentStatus{

        @Test
        void 결제_생성할_때_예약_상태가_PENDING이면_PAYMENT_IN_PROGRESS_예외_발생(){

            //given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.PENDING);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> { ReflectionTestUtils.invokeMethod(paymentService, "validatePaymentStatus", reservation); });

            assertEquals(PaymentErrorCode.PAYMENT_IN_PROGRESS, exception.getErrorCode());

        }
        @Test
        void 결제_생성할_때_예약_상태가_APPROVED이면_PAYMENT_ALREADY_APPROVED_예외_발생(){

            //given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.APPROVED);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> { ReflectionTestUtils.invokeMethod(paymentService, "validatePaymentStatus", reservation); });

            assertEquals(PaymentErrorCode.PAYMENT_ALREADY_APPROVED, exception.getErrorCode());

        }
        @Test
        void 결제_생성할_때_예약_상태가_CANCELED이면_PAYMENT_CANCELED_예외_발생(){

            //given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.CANCELED);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> { ReflectionTestUtils.invokeMethod(paymentService, "validatePaymentStatus", reservation); });

            assertEquals(PaymentErrorCode.PAYMENT_CANCELED, exception.getErrorCode());

        }
    }

    @Nested
    class validateReservationStatus {

        @Test
        void 예약상태가_CONFIRMED이면_ALREADY_APPROVED_예외_발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.CONFIRMED);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> ReflectionTestUtils.invokeMethod(paymentService, "validateReservationStatus", reservation));

            assertEquals(PaymentErrorCode.PAYMENT_ALREADY_APPROVED, exception.getErrorCode());
        }

        @Test
        void 예약상태가_COMPLETED이면_RESERVATION_ALREADY_USED_예외_발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.COMPLETED);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> ReflectionTestUtils.invokeMethod(paymentService, "validateReservationStatus", reservation));

            assertEquals(ReservationErrorCode.RESERVATION_ALREADY_USED, exception.getErrorCode());
        }

        @Test
        void 예약상태가_CANCELED이면_RESERVATION_ALREADY_CANCELED_예외_발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.CANCELED);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> ReflectionTestUtils.invokeMethod(paymentService, "validateReservationStatus", reservation));

            assertEquals(ReservationErrorCode.RESERVATION_ALREADY_CANCELED, exception.getErrorCode());
        }

        @Test
        void 예약상태가_PAYMENT_EXPIRED이면_PAYMENT_TIME_OUT_예외_발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.PAYMENT_EXPIRED);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> ReflectionTestUtils.invokeMethod(paymentService, "validateReservationStatus", reservation));

            assertEquals(PaymentErrorCode.PAYMENT_TIME_OUT, exception.getErrorCode());
        }

        @Test
        void 예약상태가_PENDING이면_예외_발생하지_않는다() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(reservation.getStatus()).willReturn(ReservationStatus.PENDING);

            // when & then
            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(paymentService, "validateReservationStatus", reservation));
        }
    }


    @Nested
    class confirmPayment {

        @Test
        void 특정_결제에_대하여_정상적으로_결제_승인_완료() {
            //given
            Long ownerId = 1L;
            Long userId = 1L;
            Long reservationId = 1L;
            Long parkingLotId = 1L;
            Long parkingZoneId = 1L;
            String orderId = "test-order-id";
            String paymentKey = "test-payment-key";
            Integer amount = 5000;

            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "paymentKey", paymentKey);
            ReflectionTestUtils.setField(request, "orderId", orderId);
            ReflectionTestUtils.setField(request, "amount", amount);

            User user = createUser(userId);

            User owner = createOwner(ownerId);
            ParkingLot parkingLot = createParkingLot(parkingLotId, owner);

            ParkingZone parkingZone = createParkingZone(parkingZoneId, parkingLot);

            ReservationRequest reservationRequest = createRequest(parkingZoneId);

            long hours = ChronoUnit.HOURS.between(reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime());
            BigDecimal price = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
            Reservation reservation = createReservation(reservationId, user, parkingZone, reservationRequest, price);


            Payment payment = Payment.builder()
                    .user(user)
                    .reservation(reservation)
                    .orderId(orderId)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
            ReflectionTestUtils.setField(payment, "id", 1L);

            PaymentConfirmResponse confirmResponse = new PaymentConfirmResponse();
            ReflectionTestUtils.setField(confirmResponse, "orderId", orderId);
            ReflectionTestUtils.setField(confirmResponse, "type", PaymentType.NORMAL);
            ReflectionTestUtils.setField(confirmResponse, "amount", amount);

            given(paymentReader.getPayment(orderId)).willReturn(payment);
            given(tossPaymentService.confirmPayment(request)).willReturn(confirmResponse);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);

            // when
            PaymentConfirmResponse result = paymentService.confirmPayment(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            verify(paymentWriter).savePayment(payment, confirmResponse);
            verify(reservationWriter).updateStatusConfirm(reservation);

        }

    }

    @Nested
    class getMyPayment{
        @Test
        void 내_결제정보_정상적으로_조회된다() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            Long paymentId = 1L;
            String orderId = "test-order-id";
            String paymentKey = "test-key";

            AuthUser authUser = createAuthUser(userId);
            User user = createUser(authUser.getId());

            Reservation reservation = mock(Reservation.class);

            Payment payment = Payment.builder()
                    .user(user)
                    .reservation(reservation)
                    .paymentType(PaymentType.NORMAL)
                    .paymentStatus(PaymentStatus.APPROVED)
                    .orderId(orderId)
                    .paymentKey(paymentKey)
                    .cardFee(0)
                    .approvedAt(LocalDateTime.of(2025, 4, 16, 14, 30))
                    .build();
            ReflectionTestUtils.setField(payment, "id", 1L);


            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));

            // when
            PaymentResponse result = paymentService.getMyPayment(authUser, reservationId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(paymentId);
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        }


        @Test
        void 결제정보가_없으면_PaymentNotFound_예외발생() {
            // given
            Long userId = 1L;
            Long reservationId = 1L;
            AuthUser authUser = createAuthUser(userId);
            Reservation reservation = mock(Reservation.class);

            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.empty());

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> paymentService.getMyPayment(authUser, reservationId));

            assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());

        }

    }

    @Nested
    class getPaymentInfo{
        @Test
        void 결제_정보_정상적으로_조회() {
            // given
            String orderId = "order-123";

            Payment payment = mock(Payment.class);
            given(payment.getUserEmail()).willReturn("test@example.com");
            given(payment.getUserNickName()).willReturn("홍길동");
            given(payment.getUserPhone()).willReturn("010-1234-5678");
            given(payment.getPrice()).willReturn(BigDecimal.valueOf(10000));
            given(payment.getUserId()).willReturn(123L);

            given(paymentReader.getPayment(orderId)).willReturn(payment);

            // when
            PaymentInfoResponse result = paymentService.getPaymentInfo(orderId);

            // then
            assertThat(result)
                    .extracting("customerEmail", "customerName", "customerMobilePhone", "price", "customerKey")
                    .containsExactly("test@example.com", "홍길동", "010-1234-5678", BigDecimal.valueOf(10000), "user_123");


        }

        @Test
        void 주문번호로_결제_정보_정상조회() {
            // given
            String orderId = "info-order-id";
            Payment payment = mock(Payment.class);
            given(paymentReader.getPayment(orderId)).willReturn(payment);
            given(payment.getUserEmail()).willReturn("test@example.com");
            given(payment.getUserNickName()).willReturn("테스터");
            given(payment.getUserPhone()).willReturn("01012345678");
            given(payment.getPrice()).willReturn(BigDecimal.valueOf(10000));
            given(payment.getUserId()).willReturn(1L);

            // when
            PaymentInfoResponse response = paymentService.getPaymentInfo(orderId);

            // then
            assertThat(response.getCustomerEmail()).isEqualTo("test@example.com");
            assertThat(response.getCustomerName()).isEqualTo("테스터");
            assertThat(response.getCustomerMobilePhone()).isEqualTo("01012345678");
            assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(10000));
            assertThat(response.getCustomerKey()).isEqualTo("user_1");
        }

        @Test
        void 주문번호로_결제정보조회시_없으면_PAYMENT_NOT_FOUND_예외_발생() {
            // given
            String orderId = "non-existent-order";
            given(paymentReader.getPayment(orderId)).willThrow(
                    new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND)
            );

            // when & then
            ParkingEasyException ex = assertThrows(ParkingEasyException.class,
                    () -> paymentService.getPaymentInfo(orderId));

            assertThat(ex.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }


    }

    @Nested
    class failPayment{

        @Test
        void 결제_실패시_결제취소되고_예약상태_정상적으로_변경() {
            // given
            String orderId = "fail-order-id";
            Long userId = 1L;
            Long reservationId = 1L;

            PaymentFailRequest request = new PaymentFailRequest();
            ReflectionTestUtils.setField(request, "orderId", orderId);
            ReflectionTestUtils.setField(request, "errorCode", "ERROR_CODE");
            ReflectionTestUtils.setField(request, "errorMessage", "결제에 실패했습니다");

            Payment payment = mock(Payment.class);
            Reservation reservation = mock(Reservation.class);

            given(paymentReader.getPayment(orderId)).willReturn(payment);
            given(payment.getUserId()).willReturn(userId);
            given(payment.getReservationId()).willReturn(reservationId);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);

            // when
            paymentService.failPayment(request);

            // then
            verify(paymentWriter).cancelPayment(payment);
            verify(reservationWriter).cancel(reservation);
        }
    }

    @Nested
    class cancelPayment {

        @Test
        void 결제정보가_없으면_PAYMENT_NOT_FOUND_예외발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.empty());
            ReservationCancelRequest request = mock(ReservationCancelRequest.class);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> paymentService.cancelPayment(reservation, request));

            assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 결제상태가_CANCELED면_PAYMENT_CANCELED_예외발생() {
            // given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.CANCELED);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));
            ReservationCancelRequest request = mock(ReservationCancelRequest.class);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> paymentService.cancelPayment(reservation, request));

            assertEquals(PaymentErrorCode.PAYMENT_CANCELED, exception.getErrorCode());
        }

        @Test
        void 결제상태가_PENDING이면_cancelPayment_정상적으로_호출() {
            // given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.PENDING);
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));
            ReservationCancelRequest request = mock(ReservationCancelRequest.class);

            // when
            paymentService.cancelPayment(reservation, request);

            // then
            verify(paymentWriter).cancelPayment(payment);
            verifyNoInteractions(tossPaymentService);
        }

        @Test
        void 결제상태가_APPROVED이면_Toss_cancel_정상적으로_호출_후_cancelPayment_정상적으로_호출() {
            // given
            Reservation reservation = mock(Reservation.class);
            Payment payment = mock(Payment.class);
            given(payment.getPaymentStatus()).willReturn(PaymentStatus.APPROVED);
            given(payment.getPaymentKey()).willReturn("payment-key-123");
            given(paymentReader.findByReservation(reservation)).willReturn(Optional.of(payment));

            ReservationCancelRequest request = mock(ReservationCancelRequest.class);

            // when
            paymentService.cancelPayment(reservation, request);

            // then
            verify(tossPaymentService).cancelPayment("payment-key-123", request);
            verify(paymentWriter).cancelPayment(payment);
        }
    }

}
