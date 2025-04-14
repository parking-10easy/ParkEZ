package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
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
    private String ParkingLotPublicDataUrl;

    @Value("${parking-lot.public-data.serviceKey}")
    private String ServiceKey;

    @Value("${parking-lot.default-image-url}")
    private String defaultParkingLotImageUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ParkingLotRepository parkingLotRepository;
    private final UserReader userReader;

    // openAPI를 통해 데이터 가져온 후 DB에 저장하기
    // TODO : 시간에 따라 데이터를 100개씩 저장하도록 설정 + 중복 데이터 처리 어떻게 할지
    public void fetchAndSavePublicData() {
        try {
            URI uri = UriComponentsBuilder.fromUriString(ParkingLotPublicDataUrl)
                    .queryParam("page", 1)
                    .queryParam("perPage", 100)
                    .queryParam("serviceKey", ServiceKey)
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
        Double latitude = Double.valueOf(data.getLatitude());
        Double longitude = Double.valueOf(data.getLongitude());
        Integer quantity = Integer.valueOf(data.getQuantity());

        String openedAtStr = data.getOpenedAt();
        LocalTime openedAt = (!StringUtils.hasText(openedAtStr))
                ? LocalTime.of(0, 0)
                : LocalTime.parse(openedAtStr);

        String closedAtStr = data.getOpenedAt();
        LocalTime closedAt = (!StringUtils.hasText(closedAtStr))
                ? LocalTime.of(0, 0)
                : LocalTime.parse(openedAtStr);

        String description = "공공데이터로 등록한 주차장입니다.";
        BigDecimal bigDecimal = BigDecimal.ZERO;
        SourceType sourceType = SourceType.PUBLIC_DATA;

        List<ParkingLotImage> images = new ArrayList<>();

        String chargeTypeStr = data.getChargeType();
        ChargeType chargeType = "무료".equals(chargeTypeStr) ? ChargeType.FREE : ChargeType.PAID;

        // TODO : 소유주 - 수정이 필요할 듯 함 (관리자로)
        User user = userReader.getActiveById(1L);

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

        ParkingLotImage defaultImage = ParkingLotImage.builder()
                .imageUrl(defaultParkingLotImageUrl)
                .build();
        defaultImage.updateParkingLot(parkingLot);
        images.add(defaultImage);

        return parkingLot;
    }
}
