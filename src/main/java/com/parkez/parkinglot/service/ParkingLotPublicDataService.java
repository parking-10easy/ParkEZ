//package com.parkez.parkinglot.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.parkez.parkinglot.client.publicData.ParkingLotData;
//import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
//import com.parkez.parkinglot.domain.entity.ParkingLot;
//import com.parkez.parkinglot.domain.entity.ParkingLotImage;
//import com.parkez.parkinglot.domain.enums.ChargeType;
//import com.parkez.parkinglot.domain.enums.SourceType;
//import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
//import com.parkez.user.domain.entity.User;
//import com.parkez.user.service.UserReader;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.math.BigDecimal;
//import java.net.URI;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//
//@Service
//@Getter
//@Slf4j
//@RequiredArgsConstructor
//public class ParkingLotPublicDataService {
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Value("${parking-lot.public-data.url}")
//    private String parkingLotPublicDataUrl;
//
//    @Value("${parking-lot.public-data.serviceKey}")
//    private String serviceKey;
//
//    @Value("${parking-lot.default-image-url}")
//    private String defaultParkingLotImageUrl;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ParkingLotRepository parkingLotRepository;
//    private final UserReader userReader;
//
//    private static final String description = "공공데이터로 등록한 주차장입니다.";
//    private int currentPage = 1;
//    private final int perPage = 10;
//
//    // TODO : jdbc template bulk insert 사용해보기
//    @Scheduled(fixedRate = 300000, initialDelay = 10000)
//    public void fetchAndSavePublicData() {
//        try {
//            URI uri = UriComponentsBuilder.fromUriString(parkingLotPublicDataUrl)
//                    .queryParam("page", currentPage)
//                    .queryParam("perPage", perPage)
//                    .queryParam("serviceKey", serviceKey)
//                    .build()
//                    .encode()
//                    .toUri();
//            log.info("요청 URL : {}", uri);
//
//            ResponseEntity<ParkingLotDataResponse> responseEntity = restTemplate.getForEntity(uri, ParkingLotDataResponse.class);
//            ParkingLotDataResponse dataResponse = responseEntity.getBody();
//
//            if (dataResponse == null || dataResponse.getData() == null) {
//                currentPage = 1;
//                log.info("받은 데이터가 없으므로, 인덱스를 1로 초기화");
//                return;
//            }
//
//            // API로 받은 모든 주차장 데이터
//            List<ParkingLotData> dataList = dataResponse.getData();
//
//            // API로 받은 모든 주차장 데이터의 주차장 이름 리스트
//            List<String> names = dataList.stream()
//                    .map(ParkingLotData::getName)
//                    .toList();
//
//            // 이름 리스트에 해당하는 주차장 데이터 조회
//            List<ParkingLot> existingParkingLots = parkingLotRepository.findByNameIn(names);
//            Map<String, ParkingLot> existingMap = existingParkingLots.stream()
//                    .collect(Collectors.toMap(ParkingLot::getName, parkingLot -> parkingLot));
//
//            // 신규 데이터와 업데이트 해야할 데이터 분리
//            List<ParkingLot> newParkingLots = new ArrayList<>();
//            List<ParkingLot> updatedParkingLots = new ArrayList<>();
//
//            for (ParkingLotData data : dataResponse.getData()) {
//                ParkingLot newParkingLot = convertToParkingLot(data);
//                // 존재하는 데이터 -> 업데이트
//                if (existingMap.containsKey(newParkingLot.getName())) {
//                    ParkingLot existingParkingLot = existingMap.get(newParkingLot.getName());
//                    existingParkingLot.update(
//                            newParkingLot.getName(),
//                            newParkingLot.getAddress(),
//                            newParkingLot.getOpenedAt(),
//                            newParkingLot.getClosedAt(),
//                            newParkingLot.getPricePerHour(),
//                            newParkingLot.getDescription(),
//                            newParkingLot.getQuantity());
//                    updatedParkingLots.add(existingParkingLot);
//                    log.info("중복 데이터 업데이트: {}", existingParkingLot.getName());
//                } else { // 새로운 데이터 -> 저장
//                    newParkingLots.add(newParkingLot);
//                    log.info("새로운 주차장 저장: {}", newParkingLot.getName());
//                }
//            }
//
//            if (!newParkingLots.isEmpty()) {
//                parkingLotRepository.saveAll(newParkingLots);
//            }
//
//            if (!updatedParkingLots.isEmpty()) {
//                parkingLotRepository.saveAll(updatedParkingLots);
//            }
//
//            if (dataResponse.getData().size() < perPage) {
//                currentPage = 1;
//                log.info("전체 데이터 저장 완료, 인덱스를 1로 초기화");
//            } else {
//                currentPage++;
//            }
//
//        } catch (Exception e) {
//            log.error("API 호출 중 에러 발생", e);
//        }
//    }
//
//    // 받아온 정보를 엔티티로 변경
//    private ParkingLot convertToParkingLot(ParkingLotData data) {
//        Double latitude = parseDouble(data.getLatitude());
//        Double longitude = parseDouble(data.getLongitude());
//        Integer quantity = parseInteger(data.getQuantity());
//        LocalTime openedAt = parseTime(data.getOpenedAt());
//        LocalTime closedAt = parseTime(data.getClosedAt());
//        BigDecimal bigDecimal = BigDecimal.ZERO;
//        SourceType sourceType = SourceType.PUBLIC_DATA;
//        ChargeType chargeType = parseChargeType(data.getChargeType());
//
//        // TODO : 소유주 - 수정이 필요할 듯 함 (관리자로)
//        User user = userReader.getActiveUserById(1L);
//
//        List<ParkingLotImage> images = new ArrayList<>();
//        ParkingLotImage defaultImage = ParkingLotImage.builder()
//                .imageUrl(defaultParkingLotImageUrl)
//                .build();
//
//        ParkingLot parkingLot = ParkingLot.builder()
//                .owner(user)
//                .name(data.getName())
//                .address(data.getAddress())
//                .latitude(latitude)
//                .longitude(longitude)
//                .openedAt(openedAt)
//                .closedAt(closedAt)
//                .pricePerHour(bigDecimal)
//                .description(description)
//                .quantity(quantity)
//                .sourceType(sourceType)
//                .images(images)
//                .chargeType(chargeType)
//                .build();
//
//        defaultImage.updateParkingLot(parkingLot);
//        images.add(defaultImage);
//
//        return parkingLot;
//    }
//
//    private LocalTime parseTime(String timeStr) {
//        if (!StringUtils.hasText(timeStr)) {
//            return LocalTime.of(0, 0);
//        }
//        return LocalTime.parse(timeStr);
//    }
//
//    private Double parseDouble(String value) {
//        try {
//            return Double.valueOf(value);
//        } catch (Exception e) {
//            log.error("Double로 변환 실패, 입력 값 : {}", value, e);
//            return null;
//        }
//    }
//
//    private Integer parseInteger(String value) {
//        try {
//            return Integer.valueOf(value);
//        } catch (Exception e) {
//            log.error("Integer로 변환 실패, 입력 값 : {}", value, e);
//            return null;
//        }
//    }
//
//    private ChargeType parseChargeType(String chargeTypeStr) {
//        if (!StringUtils.hasText(chargeTypeStr)) {
//            return null;
//        }
//        if ("무료".equalsIgnoreCase(chargeTypeStr)) {
//            return ChargeType.FREE;
//        } else if ("유료".equalsIgnoreCase(chargeTypeStr)) {
//            return ChargeType.PAID;
//        } else {
//            return null;
//        }
//    }
//}
