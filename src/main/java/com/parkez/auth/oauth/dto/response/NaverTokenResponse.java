package com.parkez.auth.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@ToString
public class NaverTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}