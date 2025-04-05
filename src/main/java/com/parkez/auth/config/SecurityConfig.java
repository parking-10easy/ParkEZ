package com.parkez.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.parkez.auth.authentication.filter.ExceptionHandlerFilter;
import com.parkez.auth.authentication.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		String[] SWAGGER_URI = {
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.index.html",
		};

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(jwtAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)
			.addFilterBefore(new ExceptionHandlerFilter(), JwtAuthenticationFilter.class)
			.formLogin(AbstractHttpConfigurer::disable)
			.anonymous(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.rememberMe(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(
				auth -> auth
					.requestMatchers(new AntPathRequestMatcher("/api/*/auth/**")).permitAll()
					.requestMatchers(SWAGGER_URI).permitAll()
					.anyRequest().authenticated()
			)
			.build();
	}

}
