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
import com.parkez.reservation.dto.request.ReservationRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private WebClient tossWebClient;

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

        System.out.println("Start DateTime: " + request.getStartDateTime());
        System.out.println("End DateTime: " + request.getEndDateTime());

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
            given(paymentWriter.createPayment(eq(user), eq(reservation), anyString())).willReturn(payment);


            //when
            PaymentCreateResponse paymentCreateResponse = paymentService.createPayment(authUser, paymentCreateRequest);

            //then
            assertThat(paymentCreateResponse).isNotNull()
                    .extracting("paymentId", "totalPrice", "orderId")
                    .containsExactly(1L, price, orderId);

        }

        @Test
        void 예약_후_결제_생성할_때_시간초과시_PAYMENT_TIME_OUT_예외발생(){
            // given
            Long userId = 1L;
            Long reservationId = 1L;

            AuthUser authUser = createAuthUser(userId);
            User user = createUser(userId);
            Reservation reservation = mock(Reservation.class);

            given(userReader.getActiveUserById(userId)).willReturn(user);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);
            given(reservation.getCreatedAt()).willReturn(LocalDateTime.now().minusMinutes(11));
            given(reservation.isTimeout(any(), anyLong())).willReturn(true);

            PaymentCreateRequest request = getPaymentCreateRequest(reservationId);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> paymentService.createPayment(authUser, request));

            assertEquals(PaymentErrorCode.PAYMENT_TIME_OUT, exception.getErrorCode());
        }

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
            ReflectionTestUtils.setField(confirmResponse, "method", "CARD");
            ReflectionTestUtils.setField(confirmResponse, "amount", amount);

            given(paymentReader.getPayment(orderId)).willReturn(payment);
            given(reservationReader.findMyReservation(userId, reservationId)).willReturn(reservation);

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();
            doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
            doReturn(Mono.just(confirmResponse)).when(responseSpec).bodyToMono(PaymentConfirmResponse.class);

            // when
            PaymentConfirmResponse result = paymentService.confirmPayment(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            verify(paymentWriter).savePayment(payment, confirmResponse);
            verify(reservationWriter).updateStatusConfirm(reservation);

        }

        @Test
        void 결제승인요청시_4xx응답이_오면_예외_발생() {
            // given
            String orderId = "fail-order-id";
            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "orderId", orderId);
            ReflectionTestUtils.setField(request, "amount", 5000);
            ReflectionTestUtils.setField(request, "paymentKey", "test-key");

            Payment payment = mock(Payment.class);
            given(paymentReader.getPayment(orderId)).willReturn(payment);

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            // WebClient 체인 mock
            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();

            doAnswer(invocation -> {
                Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);

                ClientResponse mockResponse = mock(ClientResponse.class);
                when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("테스트 에러 메시지"));

                return handler.apply(mockResponse).block();

            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> paymentService.confirmPayment(request));

            assertThat(exception.getMessage()).contains("결제 승인 실패 (4xx)");


        }

        @Test
        void 결제승인요청시_5xx응답이_오면_예외_발생() {
            // given
            String orderId = "fail-order-id-500";
            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "orderId", orderId);
            ReflectionTestUtils.setField(request, "amount", 5000);
            ReflectionTestUtils.setField(request, "paymentKey", "test-key");

            Payment payment = mock(Payment.class);
            given(paymentReader.getPayment(orderId)).willReturn(payment);

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            // WebClient 체인 mock
            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();

            doAnswer(invocation -> {
                Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);

                // 5xx 응답 조건 만족시키도록 HttpStatus.INTERNAL_SERVER_ERROR 사용
                if (predicate.test(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    ClientResponse mockResponse = mock(ClientResponse.class);
                    when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("서버 에러 발생"));

                    return handler.apply(mockResponse).block(); // 예외 던지기
                }

                return responseSpec; // 체인 유지
            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> paymentService.confirmPayment(request));

            assertThat(exception.getMessage()).contains("결제 승인 실패 (5xx)");
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

            PaymentInfoResponse response = PaymentInfoResponse.builder()
                    .customerEmail("test@example.com")
                    .customerName("홍길동")
                    .customerMobilePhone("010-1234-5678")
                    .totalPrice(BigDecimal.valueOf(10000))
                    .customerKey("customer-key-123")
                    .build();

            given(paymentReader.getPaymentInfo(orderId)).willReturn(response);

            // when
            PaymentInfoResponse result = paymentService.getPaymentInfo(orderId);

            // then
            assertThat(result).isEqualTo(response);

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
            verify(reservationWriter).updateStatusCancel(reservation);
        }



    }


}
