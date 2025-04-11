package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@Slf4j
@RequiredArgsConstructor
public class ParkingLotPublicDataService {

    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${parking-lot.public-data.url}")
    private String ParkingLotPublicDataUrl;

    @Value("${parking-lot.public-data.serviceKey}")
    private String ServiceKey;

    // openAPI를 통해 데이터 가져오기
    public void getPublicDataFromApi() {
        try {
            int firstPage = 1;
            String firstPageUrl = buildUrl(firstPage);
            String firstPageResponse = getApiResponse(firstPageUrl);

            JsonNode node = mapper.readTree(firstPageResponse);
            int totalCount = node.path("totalCount").asInt();
            int perPage = node.path("perPage").asInt();
            int totalPages = (totalCount % perPage == 0) ? (totalCount / perPage) : (totalCount / perPage + 1);

            for (int i = 2; i <= totalPages; i++) {
                String pageUrl = buildUrl(i);
                String pageResponse = getApiResponse(pageUrl);
            }
        } catch (Exception e) {
            log.error("API 호출 중 에러 발생", e);
        }
    }

    // page를 통해 url을 생성하는 메소드
    private String buildUrl(int page) {
        return ParkingLotPublicDataUrl +
                "?page=" + URLEncoder.encode(String.valueOf(page), StandardCharsets.UTF_8) +
                "&perPage=" + URLEncoder.encode("10", StandardCharsets.UTF_8) +
                "&serviceKey=" + ServiceKey;
    }

    // url을 통해 조회한 응답데이터를 반환하는 메소드
    private String getApiResponse(String urlStr) {
        HttpURLConnection connection = null;
        try {
            // 객체 생성 및 통신 시작
            URL url = new URL(urlStr);
            log.info("요청 URL : {}", urlStr);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-type", "application/json");

            int responseCode = connection.getResponseCode();
            log.info("응답 코드 : {}", responseCode);

            // 응답 여부에 따른 InputStream 선택
            InputStreamReader streamReader = new InputStreamReader(
                    (responseCode >= 200 && responseCode < 300)
                            ? connection.getInputStream()
                            : connection.getErrorStream()
            );

            // 전달 받은 데이터를 BufferedReader 객체로 저장
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            return builder.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
