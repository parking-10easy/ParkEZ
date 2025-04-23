package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.JdbcUserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ParkingLotPublicDataServiceTest {

    @Mock
    HttpClient httpClient;
    @Mock
    HttpResponse<String> httpResponse;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    SimpleKakaoGeocodeClient kakaoClient;
    @Mock
    JdbcUserReader userReader;

    private ParkingLotPublicDataService service;

    @BeforeEach
    void setUp() {
        service = new ParkingLotPublicDataService(
                "http://dummy.url",
                "dummyKey",
                "http://dummy.img",
                "admin@example.com",
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                null,  // ReflectionTestUtils 로 주입
                null
        );
        ReflectionTestUtils.setField(service, "httpClient", httpClient);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(service, "kakaoGeocodeClient", kakaoClient);
        ReflectionTestUtils.setField(service, "userReader", userReader);
    }

    private ParkingLotData sampleData() {
        return ParkingLotData.builder()
                .name("주차장 데이터")
                .address("서울시 강남구")
                .latitude("37.98765")
                .longitude("127.12345")
                .openedAt("09:00")
                .closedAt("18:00")
                .quantity("10")
                .chargeType("무료")
                .build();
    }

    private User adminUser() {
        return User.ofIdEmailRole(1L, "admin@example.com", UserRole.ROLE_ADMIN);
    }

    private void stubResponse(ParkingLotDataResponse dto) throws IOException, InterruptedException {
        given(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .willReturn(httpResponse);
        given(httpResponse.body()).willReturn("{}");
        given(objectMapper.readValue(anyString(), eq(ParkingLotDataResponse.class)))
                .willReturn(dto);
        given(kakaoClient.getAddress(anyDouble(), anyDouble()))
                .willReturn(dto.getData() != null && !dto.getData().isEmpty()
                        ? dto.getData().get(0).getAddress()
                        : "주소없음");
        given(userReader.getUserByEmailAndRole("admin@example.com", UserRole.ROLE_ADMIN))
                .willReturn(adminUser());
    }

    @Test
    void 공공데이터가_정상_흐름대로_처리된다() throws Exception {
        // given
        ParkingLotDataResponse dto = ParkingLotDataResponse.builder()
                .data(List.of(sampleData()))
                .build();
        stubResponse(dto);

        // when
        assertDoesNotThrow(() -> service.fetchAndSavePublicData());

        // then: 페이지는 perPage(2)보다 작아서 1로 초기화
        int page = (int) ReflectionTestUtils.getField(service, "currentPage");
        assertEquals(1, page);
    }

    @Test
    void 데이터응답_data_null이면_페이지가_리셋된다() throws Exception {
        // given
        ParkingLotDataResponse dto = ParkingLotDataResponse.builder()
                .data(null)
                .build();

        given(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .willReturn(httpResponse);

        given(httpResponse.body()).willReturn("{}");

        given(objectMapper.readValue(anyString(), eq(ParkingLotDataResponse.class)))
                .willReturn(dto);

        // when
        service.fetchAndSavePublicData();

        // then
        int page = (int) ReflectionTestUtils.getField(service, "currentPage");
        assertEquals(1, page);
    }

    @Test
    void 데이터수가_perPage보다_작으면_페이지가_리셋된다() throws Exception {
        // given
        ReflectionTestUtils.setField(service, "currentPage", 5);
        ParkingLotDataResponse dto = ParkingLotDataResponse.builder()
                .data(List.of(sampleData()))
                .build();
        stubResponse(dto);

        // when
        service.fetchAndSavePublicData();

        // then
        int page = (int) ReflectionTestUtils.getField(service, "currentPage");
        assertEquals(1, page);
    }

    @Test
    void API호출_실패시_IOException이_던져진다() throws Exception {
        // given
        given(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .willThrow(new IOException("API 호출 실패"));

        // when & then
        assertThrows(IOException.class, () -> service.fetchAndSavePublicData());
    }
}