package com.parkez.parkinglot.client.kakaomap.geocode;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoGeocodeClient {

    @Value("${parking-lot.kakao-map.api-key}")
    private String kakaoMapApiKey;

    private static final String baseUrl = "https://dapi.kakao.com";
    private static final String pathUrl = "/v2/local/search/address.json";
    private static final String kakaoAK = "KakaoAK ";
    private static final String query = "query";

    private final WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();

    public Geocode getGeocode(String address) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(pathUrl)
                        .queryParam(query, address)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, kakaoAK + kakaoMapApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("주소를 불러오는 Kakao Map API 요청 실패, 상태: {} 에러 본문: {}",
                                            clientResponse.statusCode(), errorBody);
                                    return Mono.error(new ParkingEasyException(ParkingLotErrorCode.KAKAO_MAP_API_ERROR));
                                })
                )
                .bodyToMono(KakaoGeocodeResponse.class)
                .blockOptional()
                .filter(response -> response.getDocuments() != null && !response.getDocuments().isEmpty())
                .map(Geocode::from)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND_ADDRESS));
    }

}
