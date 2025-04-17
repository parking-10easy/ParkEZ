package com.parkez.payment.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payment")
@Getter
@RequiredArgsConstructor
public class TossPaymentProperties {
    private final String secretKey;
    private final String baseUrl;
}
