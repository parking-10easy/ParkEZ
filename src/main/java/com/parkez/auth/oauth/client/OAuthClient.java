package com.parkez.auth.oauth.client;

import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;

public interface OAuthClient {

	 OAuthUserInfo requestUserInfo(String code);

	boolean supports(OAuthProvider provider);
}
