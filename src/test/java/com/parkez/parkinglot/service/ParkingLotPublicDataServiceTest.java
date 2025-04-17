package com.parkez.parkinglot.service;

import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;

import org.apache.commons.lang3.reflect.FieldUtils;

@ExtendWith(MockitoExtension.class)
public class ParkingLotPublicDataServiceTest {


    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserReader userReader;

    @InjectMocks
    private ParkingLotPublicDataService parkingLotPublicDataService;

    @BeforeEach
    public void setUp() throws IllegalAccessException {
        FieldUtils.writeField(parkingLotPublicDataService, "parkingLotPublicDataUrl", "http://dummy.url", true);
        FieldUtils.writeField(parkingLotPublicDataService, "serviceKey", "dummyServiceKey", true);
        FieldUtils.writeField(parkingLotPublicDataService, "defaultParkingLotImageUrl", "http://dummy.image.url", true);
        FieldUtils.writeField(parkingLotPublicDataService, "restTemplate", restTemplate, true);
    }

    private ParkingLotData getNewParkingLotData() {
        return ParkingLotData.builder()
                .name("새로운 공공데이터 주차장")
                .address("새로운 주소")
                .latitude("37.111")
                .longitude("127.111")
                .quantity("100")
                .build();
    }

    private ParkingLotData getExistingParkingLotData() {
        return ParkingLotData.builder()
                .name("존재하는 공공데이터 주차장")
                .address("존재하는 주소")
                .latitude("37.222")
                .longitude("127.222")
                .quantity("100")
                .build();
    }

    private ParkingLot getExistngParkingLot() {
        User user = getOwnerUser();
        when(userReader.getActiveUserById(1L)).thenReturn(user);
        ParkingLotData existingParkingLotData = getExistingParkingLotData();

        return ParkingLot.builder()
                .owner(user)
                .name(existingParkingLotData.getName())
                .address("저장된 주소")
                .build();
    }

    private User getOwnerUser() {
        User ownerUser = User.builder()
                .email("owner@example.com")
                .nickname("Owner")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(ownerUser, "id", 1L);
        return ownerUser;
    }

    @Nested
    class fetchAndSavePublicData {
//        @Test
//        void 신규_및_업데이트_대상_주차장_공공데이터가_정상_저장된다() {
//
//            // given
//            ParkingLotData newData = getNewParkingLotData();
//            ParkingLotData existingData = getExistingParkingLotData();
//            List<ParkingLotData> dataList = Arrays.asList(newData, existingData);
//
//            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
//                    .page(1)
//                    .perPage(10)
//                    .totalCount(10)
//                    .currentCount(2)
//                    .matchCount(2)
//                    .data(dataList)
//                    .build();
//
//            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);
//
//            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
//                    .thenReturn(responseEntity);
//
//            User user = getOwnerUser();
//            ParkingLot existingParkingLot = ParkingLot.builder()
//                    .owner(user)
//                    .name(existingData.getName())
//                    .address(existingData.getAddress())
//                    .build();
//
//            when(parkingLotRepository.findByNameIn(any()))
//                    .thenReturn(List.of(existingParkingLot));
//
//            //when
//            parkingLotPublicDataService.fetchAndSavePublicData();
//
//            //then
//            // API로 받은 데이터의 건수가 perPage(10)보다 작으므로 currentPage가 리셋
//            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
//
//            ArgumentCaptor<List<ParkingLot>> captor = ArgumentCaptor.forClass(List.class);
//            verify(parkingLotRepository, times(2)).saveAll(captor.capture());
//            List<List<ParkingLot>> savedLists = captor.getAllValues();
//
//            boolean newListFound = false;
//            boolean updatedListFound = false;
//
//            for (List<ParkingLot> list : savedLists) {
//                if (!list.isEmpty()) {
//                    String name = list.get(0).getName();
//                    if (newData.getName().equals(name)) {
//                        newListFound = true;
//                        assertEquals(1, list.size());
//                    } else if (existingData.getName().equals(name)) {
//                        updatedListFound = true;
//                        assertEquals(1, list.size());
//                    }
//                }
//            }
//            assertTrue(newListFound);
//            assertTrue(updatedListFound);
//        }

        @Test
        void 신규_주차장_공공데이터가_저장된다() {
            // given
            ParkingLotData newParkingLotData = getNewParkingLotData();
            List<ParkingLotData> dataList = List.of(newParkingLotData);

            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .page(1)
                    .perPage(10)
                    .totalCount(1)
                    .currentCount(1)
                    .matchCount(1)
                    .data(dataList)
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);

            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            when(parkingLotRepository.findByNameIn(any())).thenReturn(Collections.emptyList());

            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
            ArgumentCaptor<List<ParkingLot>> captor = ArgumentCaptor.forClass(List.class);
            verify(parkingLotRepository).saveAll(captor.capture());
            List<ParkingLot> savedList = captor.getValue();
            assertEquals(1, savedList.size());
            assertEquals(newParkingLotData.getName(), savedList.get(0).getName());

        }

        @Test
        void 공공데이터의_기존_주차장_정보가_수정되어_저장된다() {

            // given
            ParkingLotData existingParkingLotData = getExistingParkingLotData();
            List<ParkingLotData> dataList = List.of(existingParkingLotData);

            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .page(1)
                    .perPage(10)
                    .totalCount(1)
                    .currentCount(1)
                    .matchCount(1)
                    .data(dataList)
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            ParkingLot existngParkingLot = getExistngParkingLot();
            when(parkingLotRepository.findByNameIn(any())).thenReturn(List.of(existngParkingLot));

            //when
            parkingLotPublicDataService.fetchAndSavePublicData();

            //then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
            ArgumentCaptor<List<ParkingLot>> captor = ArgumentCaptor.forClass(List.class);
            verify(parkingLotRepository).saveAll(captor.capture());
            List<ParkingLot> savedList = captor.getValue();
            assertEquals(1, savedList.size());
            assertEquals(existingParkingLotData.getName(), savedList.get(0).getName());
            assertEquals(existingParkingLotData.getAddress(), savedList.get(0).getAddress());
        }

        @Test
        void 데이터_응답이_null_이면_currentPage가_리셋된다() {
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
        void 데이터의_수가_perPage보다_작으면_currentPage가_리셋된다(){
            // given
            ReflectionTestUtils.setField(parkingLotPublicDataService, "currentPage", 5);

            ParkingLotData newData = getNewParkingLotData();
            List<ParkingLotData> dataList = List.of(newData);
            ParkingLotDataResponse dataResponse = ParkingLotDataResponse.builder()
                    .page(5)
                    .perPage(10)
                    .totalCount(1)
                    .currentCount(1)
                    .matchCount(1)
                    .data(dataList)
                    .build();

            ResponseEntity<ParkingLotDataResponse> responseEntity = ResponseEntity.ok(dataResponse);
            when(restTemplate.getForEntity(any(URI.class), eq(ParkingLotDataResponse.class)))
                    .thenReturn(responseEntity);

            ParkingLot existngParkingLot = getExistngParkingLot();
            when(parkingLotRepository.findByNameIn(any())).thenReturn(List.of(existngParkingLot));

            // when
            parkingLotPublicDataService.fetchAndSavePublicData();

            // then
            assertEquals(1, parkingLotPublicDataService.getCurrentPage());
        }
    }


}
