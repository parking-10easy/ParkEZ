package com.parkez.auth.oauth.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NaverUserInfoResponse {

    private final Long id;
    private final NaverResponse response;

    @Getter
    @RequiredArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class NaverResponse {
        private final String email;
        private final String nickname;
    }


    public String getEmail() {
        return Optional.ofNullable(this.response)
                .map(NaverResponse::getEmail)
                .orElse(null);
    }

    public String getNickname() {
        return Optional.ofNullable(this.response)
                .map(NaverResponse::getNickname)
                .orElse(null);
    }
}
