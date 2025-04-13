package com.parkez.auth.oauth.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OauthClientAdapter {

	private final List<OAuthClient> OAuthClients;

	public OAuthClient getClient(OAuthProvider provider) {
		return OAuthClients.stream()
			.filter(client -> client.supports(provider))
			.findFirst()
			.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.INVALID_OAUTH_PROVIDER));
	}

}
