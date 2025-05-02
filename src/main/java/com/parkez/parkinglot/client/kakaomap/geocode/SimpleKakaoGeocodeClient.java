package com.parkez.parkinglot.client.kakaomap.geocode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SimpleKakaoGeocodeClient {

    private static final String COORD_URL = "https://dapi.kakao.com/v2/local/geo/coord2address.json";
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apikey;
    private final ObjectMapper mapper = new ObjectMapper();

    public SimpleKakaoGeocodeClient(String apikey) {
        this.apikey = apikey;
    }

    public String getAddress(Double longitude, Double latitude) {
        try {
            URI uri = URI.create(COORD_URL + "?x=" + longitude + "&y=" + latitude);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Authorization", "KakaoAK " + apikey)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode docs = mapper.readTree(response.body()).path("documents");
            if (docs.isEmpty() || docs.isNull()) {
                return "좌표로 주소를 찾을 수 없습니다.";
            }
            return docs.get(0).path("address").path("address_name").asText();

        } catch (Exception e) {
            throw new RuntimeException("Kakao API 호출 실패", e);
        }
    }

}
