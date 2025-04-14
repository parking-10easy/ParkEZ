package com.parkez.payment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(PaymentProperties.class)
public class TossPaymentClient {

    private static final String AUTH_HEADER_PREFIX = "Basic ";

    private String createPaymentAuthHeader(PaymentProperties paymentProperties) {
        return AUTH_HEADER_PREFIX + Base64.getEncoder().encodeToString((paymentProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public WebClient tossWebClient(PaymentProperties paymentProperties) {
        String authHeader = createPaymentAuthHeader(paymentProperties);

        return WebClient.builder()
                .baseUrl(paymentProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    log.info("====== WebClient 요청 로그 ======");
                    log.info("요청 메서드: " + request.method());
                    log.info("요청 URL: " + request.url());
                    request.headers().forEach((name, values) ->
                            values.forEach(value ->
                                    log.info("헤더 - " + name + ": " + value)));
                    return next.exchange(request);
                })
                .build();
    }
}
