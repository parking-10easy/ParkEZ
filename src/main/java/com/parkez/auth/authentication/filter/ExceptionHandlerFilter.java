package com.parkez.auth.authentication.filter;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.common.exception.ErrorCode;
import com.parkez.common.exception.ErrorResponse;
import com.parkez.common.exception.ParkingEasyException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		}catch (ParkingEasyException e) {
			log.error("[ExceptionHandlerFilter] ParkingEasyException 발생: {}", e.getMessage(), e);
			setErrorResponse(response, e.getErrorCode());
		}
	}

	private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ErrorResponse errorResponse = ErrorResponse.of(errorCode);

		PrintWriter writer = response.getWriter();
		writer.write(objectMapper.writeValueAsString(errorResponse));
		writer.flush();

	}
}
