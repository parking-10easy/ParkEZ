package com.parkez.auth.authentication.jwt;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {


	public static final String BEARER_PREFIX = "Bearer ";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	private final SecretKey signingKey;
	private final JwtProperties jwtProperties;

	public JwtProvider(JwtProperties jwtProperties) {
		byte[] bytes = Base64.getDecoder().decode(jwtProperties.getSecretKey());
		this.signingKey = Keys.hmacShaKeyFor(bytes);
		this.jwtProperties = jwtProperties;
	}

	public String createAccessToken(Long userId, String email, String roleName, String nickname) {
		Date date = new Date();
		return BEARER_PREFIX +
			Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("userRole", roleName)
				.claim("nickname", nickname)
				.expiration(createExpiration(jwtProperties.getAccessTokenExpiration()))
				.issuedAt(date)
				.signWith(this.signingKey)
				.compact();
	}

	public String createRefreshToken(Long userId) {
		Date date = new Date();

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.expiration(createExpiration(jwtProperties.getRefreshTokenExpiration()))
			.issuedAt(date)
			.signWith(this.signingKey)
			.compact();
	}

	public Claims extractClaims(String token) {
		return Jwts.parser()
			.verifyWith(this.signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean isTokenExpired(String token) {
		try {
			Claims claims = extractClaims(token);
			Date expirationDate = claims.getExpiration();
			return expirationDate != null && expirationDate.before(new Date());
		} catch (Exception e) {
			return true;
		}
	}

	public String subStringToken(String tokenValue) {
		if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
			return tokenValue.substring(BEARER_PREFIX.length());
		}
		throw new ParkingEasyException(AuthErrorCode.TOKEN_NOT_FOUND);
	}

	private Date createExpiration(long tokenTime) {
		return new Date(System.currentTimeMillis() + tokenTime);
	}

}
