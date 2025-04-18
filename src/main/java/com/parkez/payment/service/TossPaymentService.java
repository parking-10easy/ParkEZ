package com.parkez.payment.service;

import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final WebClient tossWebClient;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        return tossWebClient.post()
                .uri("/confirm")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("클라이언트 오류: {}", body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (4xx): " + body));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("서버 오류: {}", body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (5xx): " + body));
                        })
                )
                .bodyToMono(PaymentConfirmResponse.class)
                .block();
    }

    public void cancelPayment(String paymentKey, ReservationCancelRequest request) {
        tossWebClient.post()
                .uri("/{paymentKey}/cancel", paymentKey)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("클라이언트 오류: {}", body);
                            return Mono.error(new RuntimeException("결제 취소 실패 (4xx): " + body));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("서버 오류: {}", body);
                            return Mono.error(new RuntimeException("결제 취소 실패 (5xx): " + body));
                        })
                )
                .bodyToMono(Void.class)
                .block();
    }


}
