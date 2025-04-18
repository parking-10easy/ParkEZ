package com.parkez.parkinglot.service;

import com.parkez.parkinglot.client.kakaomap.geocode.KakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingLotPublicDataServiceTest {


    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserReader userReader;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private KakaoGeocodeClient kakaoGeocodeClient;

    @InjectMocks
    private ParkingLotPublicDataService parkingLotPublicDataService;

    @BeforeEach
    public void setUp() throws IllegalAccessException {
        ReflectionTestUtils.setField(parkingLotPublicDataService, "adminEmail", "admin@example.com");
        FieldUtils.writeField(parkingLotPublicDataService, "parkingLotPublicDataUrl", "http://dummy.url", true);
        FieldUtils.writeField(parkingLotPublicDataService, "serviceKey", "dummyServiceKey", true);
        FieldUtils.writeField(parkingLotPublicDataService, "defaultParkingLotImageUrl", "http://dummy.image.url", true);
        FieldUtils.writeField(parkingLotPublicDataService, "restTemplate", restTemplate, true);
    }

    private ParkingLotData getParkingLotData() {
        return ParkingLotData.builder()
                .name("주차장 데이터")
                .address("서울시 강남구")
                .latitude("127.12345")
                .longitude("37.98765")
                .openedAt("09:00")
                .closedAt("18:00")
                .quantity("10")
                .chargeType("무료")
                .build();
    }

    private User getAdminUser() {
        User ownerUser = User.builder()
                .email("admin@example.com")
                .nickname("관리자")
                .role(UserRole.ROLE_ADMIN)
                .build();
        ReflectionTestUtils.setField(ownerUser, "id", 1L);
        return ownerUser;
    }

    @Nested
    class fetchAndSavePublicData {
        @Test
        void 공공데이터가_정상_저장된다() {

            // given
            ParkingLotData data = getParkingLotData();

            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .data(List.of(data))
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);

            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            when(kakaoGeocodeClient.getAddress(any(), any())).thenReturn(data.getAddress());

            User adminUser = getAdminUser();
            when(userReader.getUserByEmailAndRole(adminUser.getEmail(), adminUser.getRole()))
                    .thenReturn(adminUser);

            //when
            parkingLotPublicDataService.fetchAndSavePublicData();

            //then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
            verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }

        @Test
        void DB에_있는_공공데이터는_저장되지_않는다() {
            // given
            ParkingLotData data = getParkingLotData();

            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .data(List.of(data))
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);

            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            when(kakaoGeocodeClient.getAddress(any(), any())).thenReturn(data.getAddress());

            User adminUser = getAdminUser();
            when(userReader.getUserByEmailAndRole(adminUser.getEmail(), adminUser.getRole()))
                    .thenReturn(adminUser);

            doThrow(DataIntegrityViolationException.class)
                    .when(jdbcTemplate).batchUpdate(contains("INSERT INTO parking_lot"), any(BatchPreparedStatementSetter.class));

            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            verify(jdbcTemplate, times(1))
                    .batchUpdate(contains("INSERT INTO parking_lot"), any(BatchPreparedStatementSetter.class));

            verify(jdbcTemplate, never())
                    .batchUpdate(contains("INSERT INTO parking_lot_image"), any(BatchPreparedStatementSetter.class));
        }

        @Test
        void 데이터_응답이_null_이면_저장되지_않고_페이지가_초기화된다() {
            // given
            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(ResponseEntity.ok(null));

            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
        }

        @Test
        void 응답_데이터의_데이터_필드가_null_일_때_currentPage가_리셋된다() {
            // given
            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .page(1)
                    .perPage(10)
                    .totalCount(0)
                    .currentCount(0)
                    .matchCount(0)
                    .data(null)
                    .build();

            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(ResponseEntity.ok(dataResponse));

            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());

        }

        @Test
        void 데이터의_수가_perPage보다_작으면_currentPage가_리셋된다() {
            // given
            ReflectionTestUtils.setField(parkingLotPublicDataService, "currentPage", 5);
            ParkingLotData data = getParkingLotData();

            List<ParkingLotData> dataList = List.of(data);
            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .data(dataList)
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            when(kakaoGeocodeClient.getAddress(any(), any())).thenReturn(data.getAddress());

            User adminUser = getAdminUser();
            when(userReader.getUserByEmailAndRole(adminUser.getEmail(), adminUser.getRole()))
                    .thenReturn(adminUser);

            when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                    .thenReturn(new int[]{1});
            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
        }


        @Test
        void API_호출_실패시_예외가_발생한다() {
            // given
            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenThrow(new RuntimeException("API 호출 실패"));

            // when, then
            assertThrows(RuntimeException.class, () -> parkingLotPublicDataService.fetchAndSavePublicData());
        }

    }
}
