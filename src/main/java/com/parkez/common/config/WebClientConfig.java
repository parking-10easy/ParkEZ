package com.parkez.common.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	private static final int CONNECTION_TIMEOUT = 10000; // 10초

	@Bean
	public WebClient webClient() {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
			.responseTimeout(Duration.ofMillis(CONNECTION_TIMEOUT));

		return WebClient.builder()
			.uriBuilderFactory(factory)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}
}
