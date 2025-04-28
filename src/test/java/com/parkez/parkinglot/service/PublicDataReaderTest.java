package com.parkez.parkinglot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublicDataReaderTest {


    @Mock HttpClient httpClient;
    @Mock ObjectMapper mapper;
    @Mock HttpResponse<String> httpResponse;

    @InjectMocks
    private PublicDataReader reader = new PublicDataReader("https://example.com", "KEY");


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reader, "http",   httpClient);
        ReflectionTestUtils.setField(reader, "mapper", mapper);
    }


    @Nested
    class fetchPage {

        @Test
        void 성공적으로_공공데이터를_가져온다() throws Exception {
            // given
            List<ParkingLotData> dataList = singletonList(new ParkingLotData());
            ParkingLotDataResponse dto = ParkingLotDataResponse
                    .builder()
                    .data(dataList)
                    .build();

            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.body()).thenReturn("{ }");  // 실제 파싱 무시
            when(mapper.readValue(anyString(), eq(ParkingLotDataResponse.class)))
                    .thenReturn(dto);

            // when
            List<ParkingLotData> result = reader.fetchPage(1);

            // then
            assertEquals(dataList, result);
        }
    }


    @Test
    void 네트워크_오류시_예외를_던진다() throws Exception {
        // given
        when(httpClient.send(any(), any()))
                .thenThrow(new IOException("네트워크 에러"));

        // when
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> reader.fetchPage(1)
        );

        // then
        assertTrue(ex.getMessage().contains("공공데이터 fetch 실패"));
    }
}
