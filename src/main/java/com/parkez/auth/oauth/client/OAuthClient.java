package com.parkez.auth.oauth.client;

import com.parkez.auth.oauth.dto.response.OAuthUserInfo;

public interface OAuthClient {

	 OAuthUserInfo requestUserInfo(String code);

	boolean supports(String providerName);
}
