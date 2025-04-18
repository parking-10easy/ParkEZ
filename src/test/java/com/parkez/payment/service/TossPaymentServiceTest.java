package com.parkez.payment.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
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

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TossPaymentServiceTest {

    @InjectMocks
    private TossPaymentService tossPaymentService;

    @Mock
    private WebClient tossWebClient;

    @Nested
    class confirmPayment {
        @Test
        void 결제승인요청시_4xx응답이_반환되면_예외_발생() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "orderId", "fail-order-id");
            ReflectionTestUtils.setField(request, "amount", 5000);
            ReflectionTestUtils.setField(request, "paymentKey", "test-key");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();

            // 4xx 응답 시 예외 발생 처리
            doAnswer(invocation -> {
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);
                ClientResponse mockResponse = mock(ClientResponse.class);
                when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("테스트 에러 메시지"));
                return handler.apply(mockResponse).block();
            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tossPaymentService.confirmPayment(request));

            assertThat(exception.getMessage()).contains("결제 승인 실패 (4xx)");
        }

        @Test
        void 결제승인요청시_5xx응답이_반환되면_예외_발생() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "orderId", "fail-order-id-500");
            ReflectionTestUtils.setField(request, "amount", 5000);
            ReflectionTestUtils.setField(request, "paymentKey", "test-key");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();

            // 5xx 응답 시 예외 발생 처리
            doAnswer(invocation -> {
                Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);

                if (predicate.test(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    ClientResponse mockResponse = mock(ClientResponse.class);
                    when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("서버 에러 발생"));
                    return handler.apply(mockResponse).block();
                }

                return responseSpec;
            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tossPaymentService.confirmPayment(request));

            assertThat(exception.getMessage()).contains("결제 승인 실패 (5xx)");
        }

        @Test
        void 결제승인요청시_성공하면_응답객체_정상적으로_반환() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest();
            ReflectionTestUtils.setField(request, "orderId", "success-order-id");
            ReflectionTestUtils.setField(request, "amount", 10000);
            ReflectionTestUtils.setField(request, "paymentKey", "success-key");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            PaymentConfirmResponse expectedResponse = new PaymentConfirmResponse();
            ReflectionTestUtils.setField(expectedResponse, "orderId", "success-order-id");
            ReflectionTestUtils.setField(expectedResponse, "amount", 10000);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/confirm");
            doReturn(bodySpec).when(bodySpec).bodyValue(any());
            doReturn(responseSpec).when(bodySpec).retrieve();
            doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
            doReturn(Mono.just(expectedResponse)).when(responseSpec).bodyToMono(PaymentConfirmResponse.class);

            // when
            PaymentConfirmResponse result = tossPaymentService.confirmPayment(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo("success-order-id");
            assertThat(result.getAmount()).isEqualTo(10000);
        }
    }


    @Nested
    class cancelPayment{
        @Test
        void 결제취소요청_성공시_예외없이_정상적으로_완료() {
            // given
            String paymentKey = "cancel-key";
            ReservationCancelRequest request = new ReservationCancelRequest();
            ReflectionTestUtils.setField(request, "cancelReason", "테스트 취소");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/{paymentKey}/cancel", paymentKey);
            doReturn(bodySpec).when(bodySpec).bodyValue(request);
            doReturn(responseSpec).when(bodySpec).retrieve();
            doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
            doReturn(Mono.empty()).when(responseSpec).bodyToMono(Void.class);

            // when & then
            assertDoesNotThrow(() -> tossPaymentService.cancelPayment(paymentKey, request));
        }

        @Test
        void 결제취소요청시_4xx응답이_반환되면_예외_발생() {
            // given
            String paymentKey = "cancel-4xx";
            ReservationCancelRequest request = new ReservationCancelRequest();
            ReflectionTestUtils.setField(request, "cancelReason", "잘못된 요청");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/{paymentKey}/cancel", paymentKey);
            doReturn(bodySpec).when(bodySpec).bodyValue(request);
            doReturn(responseSpec).when(bodySpec).retrieve();

            doAnswer(invocation -> {
                Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);

                if (predicate.test(HttpStatus.BAD_REQUEST)) {
                    ClientResponse mockResponse = mock(ClientResponse.class);
                    when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("잘못된 요청입니다"));
                    return handler.apply(mockResponse).block();
                }
                return responseSpec;
            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tossPaymentService.cancelPayment(paymentKey, request));

            assertThat(exception.getMessage()).contains("결제 취소 실패 (4xx)");
        }

        @Test
        void 결제취소요청시_5xx응답이_반환되면_예외_발생() {
            // given
            String paymentKey = "cancel-5xx";
            ReservationCancelRequest request = new ReservationCancelRequest();
            ReflectionTestUtils.setField(request, "cancelReason", "서버 내부 에러");

            WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

            doReturn(uriSpec).when(tossWebClient).post();
            doReturn(bodySpec).when(uriSpec).uri("/{paymentKey}/cancel", paymentKey);
            doReturn(bodySpec).when(bodySpec).bodyValue(request);
            doReturn(responseSpec).when(bodySpec).retrieve();

            doAnswer(invocation -> {
                Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
                Function<ClientResponse, Mono<? extends Throwable>> handler = invocation.getArgument(1);

                if (predicate.test(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    ClientResponse mockResponse = mock(ClientResponse.class);
                    when(mockResponse.bodyToMono(String.class)).thenReturn(Mono.just("서버 에러 발생"));
                    return handler.apply(mockResponse).block();
                }
                return responseSpec;
            }).when(responseSpec).onStatus(any(), any());

            // when & then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tossPaymentService.cancelPayment(paymentKey, request));

            assertThat(exception.getMessage()).contains("결제 취소 실패 (5xx)");
        }
    }

}
