package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Getter
@Slf4j
@RequiredArgsConstructor
public class ParkingLotPublicDataService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${parking-lot.public-data.url}")
    private String parkingLotPublicDataUrl;

    @Value("${parking-lot.public-data.serviceKey}")
    private String serviceKey;

    @Value("${parking-lot.default-image-url}")
    private String defaultParkingLotImageUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ParkingLotRepository parkingLotRepository;
    private final UserReader userReader;

    private static final String description = "공공데이터로 등록한 주차장입니다.";

    // openAPI를 통해 데이터 가져온 후 DB에 저장하기
    // TODO : 시간에 따라 데이터를 100개씩 저장하도록 설정 + 중복 데이터 처리 어떻게 할지
    public void fetchAndSavePublicData() {
        try {
            URI uri = UriComponentsBuilder.fromUriString(parkingLotPublicDataUrl)
                    .queryParam("page", 1)
                    .queryParam("perPage", 100)
                    .queryParam("serviceKey", serviceKey)
                    .build()
                    .encode()
                    .toUri();
            log.info("요청 URL : {}", uri);

            ResponseEntity<ParkingLotDataResponse> responseEntity = restTemplate.getForEntity(uri, ParkingLotDataResponse.class);
            ParkingLotDataResponse dataResponse = responseEntity.getBody();

            if (dataResponse != null && dataResponse.getData() != null) {
                List<ParkingLot> parkingLotList = dataResponse.getData().stream().map(this::convertToParkingLot).toList();
                parkingLotRepository.saveAll(parkingLotList);
                log.info("DB에 저장한 데이터 수 : {}", parkingLotList.size());
            }

        } catch (Exception e) {
            log.error("API 호출 중 에러 발생", e);
        }
    }

    // 받아온 정보를 엔티티로 변경
    private ParkingLot convertToParkingLot(ParkingLotData data) {
        Double latitude = parseDouble(data.getLatitude());
        Double longitude = parseDouble(data.getLongitude());
        Integer quantity = parseInteger(data.getQuantity());
        LocalTime openedAt = parseTime(data.getOpenedAt());
        LocalTime closedAt = parseTime(data.getClosedAt());
        BigDecimal bigDecimal = BigDecimal.ZERO;
        SourceType sourceType = SourceType.PUBLIC_DATA;
        ChargeType chargeType = parseChargeType(data.getChargeType());

        // TODO : 소유주 - 수정이 필요할 듯 함 (관리자로)
        User user = userReader.getActiveUserById(1L);

        List<ParkingLotImage> images = new ArrayList<>();
        ParkingLotImage defaultImage = ParkingLotImage.builder()
                .imageUrl(defaultParkingLotImageUrl)
                .build();

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(user)
                .name(data.getName())
                .address(data.getAddress())
                .latitude(latitude)
                .longitude(longitude)
                .openedAt(openedAt)
                .closedAt(closedAt)
                .pricePerHour(bigDecimal)
                .description(description)
                .quantity(quantity)
                .sourceType(sourceType)
                .images(images)
                .chargeType(chargeType)
                .build();

        defaultImage.updateParkingLot(parkingLot);
        images.add(defaultImage);

        return parkingLot;
    }

    private LocalTime parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return LocalTime.of(0, 0);
        }
        return LocalTime.parse(timeStr);
    }

    private Double parseDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            log.error("Double로 변환 실패, 입력 값 : {}", value, e);
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            log.error("Integer로 변환 실패, 입력 값 : {}", value, e);
            return null;
        }
    }

    private ChargeType parseChargeType(String chargeTypeStr) {
        if (!StringUtils.hasText(chargeTypeStr)) {
            return null;
        }
        if ("무료".equalsIgnoreCase(chargeTypeStr)) {
            return ChargeType.FREE;
        } else if ("유료".equalsIgnoreCase(chargeTypeStr)) {
            return ChargeType.PAID;
        } else {
            return null;
        }
    }
}
