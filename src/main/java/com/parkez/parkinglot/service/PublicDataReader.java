package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class PublicDataReader {
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String serviceKey;
    private final int perPage = 2;

    public PublicDataReader(String baseUrl, String serviceKey) {
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    public List<ParkingLotData> fetchPage(int page) {
        try {
            // 공공 데이터 호출
            URI uri = new URIBuilder(baseUrl)
                    .addParameter("page", String.valueOf(page))
                    .addParameter("perPage", String.valueOf(perPage))
                    .addParameter("serviceKey", serviceKey)
                    .build();

            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            String body = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ParkingLotDataResponse response = mapper.readValue(
                    body, ParkingLotDataResponse.class
            );
            return response.getData() != null ? response.getData() : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("공공데이터 fetch 실패", e);
        }
    }
}
