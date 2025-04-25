package com.parkez.parkinglot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.client.publicData.ParkingLotDataResponse;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.JdbcUserReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ParkingLotPublicDataService {


    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String parkingLotPublicDataUrl;
    private final String serviceKey;
    private final String defaultParkingLotImageUrl;
    private final String adminEmail;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final SimpleKakaoGeocodeClient kakaoGeocodeClient;

    private final JdbcUserReader userReader;

    private static final String description = "공공데이터로 등록한 주차장입니다.";
    private int currentPage = 1;
    private final int perPage = 2;

    public ParkingLotPublicDataService(
            String dataUrl,
            String serviceKey,
            String defaultImg,
            String adminEmail,
            String jdbcUrl,
            String dbUser,
            String dbPassword,
            SimpleKakaoGeocodeClient kakaoClient,
            JdbcUserReader userReader
    ) {
        this.parkingLotPublicDataUrl = dataUrl;
        this.serviceKey = serviceKey;
        this.defaultParkingLotImageUrl = defaultImg;
        this.adminEmail = adminEmail;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.kakaoGeocodeClient = kakaoClient;
        this.userReader = userReader;
    }

    public void fetchAndSavePublicData() throws IOException, InterruptedException, URISyntaxException {
        try {
            URI uri = new URIBuilder(parkingLotPublicDataUrl)
                    .addParameter("page", String.valueOf(currentPage))
                    .addParameter("perPage", String.valueOf(perPage))
                    .addParameter("serviceKey", serviceKey)
                    .build();

            // HTTP 호출
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // json -> dto로 변환
            ParkingLotDataResponse dataResponse = objectMapper.readValue(
                    response.body(),
                    ParkingLotDataResponse.class
            );

            // 페이지 초기화
            List<ParkingLotData> dataList = dataResponse.getData();
            if (dataList == null || dataList.isEmpty()) {
                currentPage = 1;
                return;
            }

            // dto -> entity 변환
            List<ParkingLot> parkingLots = dataList.stream()
                    .map(this::convertToParkingLot)
                    .toList();

            // db에 저장
            try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                connection.setAutoCommit(false);
                try {
                    bulkInsertParkingLots(connection, parkingLots);
                    bulkInsertImages(connection, parkingLots);
                    connection.commit();
                    log.info("DB 저장 완료: {}건", parkingLots.size());
                } catch (SQLException e) {
                    connection.rollback();
                    log.warn("DB 저장 중 오류, 롤백 처리함", e);
                }
            } catch (Exception e) {
                throw new RuntimeException("공공데이터 fetch & save 실패", e);
            }

            // 페이지 인덱스 갱신
            currentPage = (dataList.size() < perPage) ? 1 : currentPage + 1;

        } catch (Exception e) {
            throw e;
        }
    }

    private void bulkInsertParkingLots(Connection connection, List<ParkingLot> parkingLots) throws SQLException {
        String sql = """
                INSERT INTO parking_lot
                  (owner_id, name, address, latitude, longitude,
                   opened_at, closed_at, price_per_hour,
                   description, quantity, charge_type,
                   source_type, status, created_at,  modified_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                 ON DUPLICATE KEY UPDATE id = id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (ParkingLot pl : parkingLots) {
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
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void bulkInsertImages(Connection connection, List<ParkingLot> parkingLots) throws SQLException {
        String sql = """
                INSERT IGNORE INTO parking_lot_image
                  (parking_lot_id, image_url, created_at, modified_at)
                SELECT pl.id, ?, NOW(), NOW()
                  FROM parking_lot pl
                 WHERE pl.longitude = ?
                   AND pl.latitude = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (ParkingLot pl : parkingLots) {
                ps.setString(1, pl.getImages().get(0).getImageUrl());
                ps.setDouble(2, pl.getLongitude());
                ps.setDouble(3, pl.getLatitude());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // 받아온 정보를 엔티티로 변경
    private ParkingLot convertToParkingLot(ParkingLotData data) {
        Double latitude = parseDouble(data.getLatitude());
        Double longitude = parseDouble(data.getLongitude());

        String getAddress = kakaoGeocodeClient.getAddress(longitude, latitude);
        String address = (data.getAddress() != null && !data.getAddress().isBlank()) ? data.getAddress() : getAddress;
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
        if (timeStr == null || timeStr.isEmpty()) {
            return LocalTime.of(0, 0);
        }
        return LocalTime.parse(timeStr);
    }

    private Double parseDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
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
