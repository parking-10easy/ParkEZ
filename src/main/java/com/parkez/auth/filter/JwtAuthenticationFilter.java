package com.parkez.auth.filter;

import static com.parkez.auth.jwt.JwtProvider.*;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.jwt.JwtAuthenticationToken;
import com.parkez.auth.jwt.JwtProvider;
import com.parkez.auth.model.AuthUser;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.enums.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
			String accessToken = jwtProvider.subStringToken(authorizationHeader);

			try {
				Claims claims = jwtProvider.extractClaims(accessToken);

				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					setAuthentication(claims);
				}
			} catch (SecurityException | MalformedJwtException e) {
				log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
				throw new ParkingEasyException(AuthErrorCode.INVALID_JWT_SIGNATURE);
			} catch (ExpiredJwtException e) {
				log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
				throw new ParkingEasyException(AuthErrorCode.TOKEN_EXPIRED);
			} catch (UnsupportedJwtException e) {
				log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
				throw new ParkingEasyException(AuthErrorCode.UNSUPPORTED_TOKEN);
			} catch (Exception e) {
				log.error("Internal server error", e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

		filterChain.doFilter(request,response);
	}

	private void setAuthentication(Claims claims) {
		Long userId = Long.valueOf(claims.getSubject());
		String email = claims.get("email", String.class);
		UserRole userRole = UserRole.of(claims.get("userRole", String.class));
		String nickname = claims.get("nickname", String.class);

		AuthUser authUser = new AuthUser(userId, email, userRole, nickname);
		JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}
}
