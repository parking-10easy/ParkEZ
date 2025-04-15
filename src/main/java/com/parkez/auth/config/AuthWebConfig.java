package com.parkez.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.parkez.auth.oauth.converter.StringToOAuthProviderConverter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthWebConfig implements WebMvcConfigurer {

	private final StringToOAuthProviderConverter stringToOAuthProviderConverter;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(stringToOAuthProviderConverter);
	}
}
