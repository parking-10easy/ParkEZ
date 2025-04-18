package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.kakaomap.geocode.KakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
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

    private final JdbcTemplate jdbcTemplate;

    private static final String description = "공공데이터로 등록한 주차장입니다.";
    private int currentPage = 1;
    private final int perPage = 2;

    @Value("${parking-lot.public-data.admin-email}")
    private String adminEmail;

    private final KakaoGeocodeClient kakaoGeocodeClient;

    @Transactional
    public void fetchAndSavePublicData() {
        try {
            URI uri = UriComponentsBuilder.fromUriString(parkingLotPublicDataUrl)
                    .queryParam("page", currentPage)
                    .queryParam("perPage", perPage)
                    .queryParam("serviceKey", serviceKey)
                    .build()
                    .encode()
                    .toUri();
            log.info("공공데이터 요청 URL : {}", uri);

            ResponseEntity<ParkingLotDataResponse> responseEntity = restTemplate.getForEntity(uri, ParkingLotDataResponse.class);
            ParkingLotDataResponse dataResponse = responseEntity.getBody();

            if (dataResponse == null || dataResponse.getData() == null) {
                currentPage = 1;
                log.info("받은 데이터가 없으므로, 인덱스를 1로 초기화");
                return;
            }

            List<ParkingLotData> dataList = dataResponse.getData();
            List<ParkingLot> parkingLots = dataList.stream()
                    .map(this::convertToParkingLot)
                    .toList();

            long start = System.currentTimeMillis();
            int totalCount = parkingLots.size();
            boolean hasDuplicate = false;

            try {
//            parkingLotRepository.saveAll(parkingLots);
                bulkInsertParkingLots(parkingLots);
                bulkInsertImages(parkingLots);
            } catch (DataIntegrityViolationException e) {
                hasDuplicate = true;;
                log.warn("중복된 위/경도를 가진 주차장이 있어 일부 저장되지 않았습니다: {}", e.getMessage());
            }

            long end = System.currentTimeMillis();

            if (hasDuplicate) {
                log.info("불러온 공공데이터 {}건 중 일부는 중복으로 저장되지 않음, 수행시간 : {}ms", totalCount, (end - start));
            } else {
                log.info("불러온 공공데이터 {}건 전부 저장 완료, 수행시간 : {}ms", totalCount, (end - start));
            }

            currentPage = (dataList.size() < perPage) ? 1 : currentPage + 1;

        } catch (Exception e) {
            log.error("API 호출 중 에러 발생", e);
            throw e;
        }
    }

    private void bulkInsertParkingLots(List<ParkingLot> parkingLots) {
        String sql = """
                INSERT INTO parking_lot
                  (owner_id, name, address, latitude, longitude,
                   opened_at, closed_at, price_per_hour,
                   description, quantity, charge_type,
                   source_type, status, created_at,  modified_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ParkingLot pl = parkingLots.get(i);
                ps.setLong(1, pl.getOwner().getId());
                ps.setString(2, pl.getName());
                ps.setString(3, pl.getAddress());
                ps.setDouble(4, pl.getLatitude());
                ps.setDouble(5, pl.getLongitude());
                ps.setTime(6, Time.valueOf(pl.getOpenedAt()));
                ps.setTime(7, Time.valueOf(pl.getClosedAt()));
                ps.setBigDecimal(8, pl.getPricePerHour());
                ps.setString(9, pl.getDescription());
                ps.setInt(10, pl.getQuantity());
                ps.setString(11, pl.getChargeType().name());
                ps.setString(12, pl.getSourceType().name());
                ps.setString(13, pl.getStatus().name());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(14, now);
                ps.setTimestamp(15, now);
            }

            @Override
            public int getBatchSize() {
                return parkingLots.size();
            }
        });
    }

    private void bulkInsertImages(List<ParkingLot> parkingLots) {
        String sql = """
                INSERT INTO parking_lot_image
                  (parking_lot_id, image_url, created_at, modified_at)
                SELECT pl.id, ?, NOW(), NOW()
                  FROM parking_lot pl
                 WHERE pl.longitude = ?
                  AND pl.latitude = ?
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ParkingLot pl = parkingLots.get(i);
                ps.setString(1, pl.getImages().get(0).getImageUrl());
                ps.setDouble(2, pl.getLongitude());
                ps.setDouble(3, pl.getLatitude());
            }

            @Override
            public int getBatchSize() {
                return parkingLots.size();
            }
        });
    }

    // 받아온 정보를 엔티티로 변경
    private ParkingLot convertToParkingLot(ParkingLotData data) {
        Double latitude = parseDouble(data.getLatitude());
        Double longitude = parseDouble(data.getLongitude());

        String getAddress = kakaoGeocodeClient.getAddress(longitude, latitude);
        String address = !StringUtils.hasText(data.getAddress()) ? getAddress : data.getAddress();
        Integer quantity = parseInteger(data.getQuantity());
        LocalTime openedAt = parseTime(data.getOpenedAt());
        LocalTime closedAt = parseTime(data.getClosedAt());
        BigDecimal bigDecimal = BigDecimal.ZERO;
        SourceType sourceType = SourceType.PUBLIC_DATA;
        ChargeType chargeType = parseChargeType(data.getChargeType());

        User user = userReader.getUserByEmailAndRole(adminEmail, UserRole.ROLE_ADMIN);

        List<ParkingLotImage> images = new ArrayList<>();
        ParkingLotImage defaultImage = ParkingLotImage.builder()
                .imageUrl(defaultParkingLotImageUrl)
                .build();

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(user)
                .name(data.getName())
                .address(address)
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
        if ("무료".equalsIgnoreCase(chargeTypeStr)) {
            return ChargeType.FREE;
        } else if ("유료".equalsIgnoreCase(chargeTypeStr)) {
            return ChargeType.PAID;
        } else {
            return ChargeType.NO_DATA;
        }
    }
}
