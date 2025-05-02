package com.parkez.parkinglot.service;

import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.PageStateRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.JdbcUserReader;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PublicDataWriter {

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final JdbcUserReader userReader;
    private final SimpleKakaoGeocodeClient kakaoGeocodeClient;
    private final PageStateRepository pageStateRepo;
    private final String adminEmail;
    private final String defaultParkingLotImageUrl;

    private static final String description = "공공데이터로 등록한 주차장입니다.";

    public PublicDataWriter(String jdbcUrl, String dbUser, String dbPassword,
                            JdbcUserReader userReader, SimpleKakaoGeocodeClient kakaoGeocodeClient,
                            PageStateRepository pageStateRepo, String adminEmail,
                            String defaultParkingLotImageUrl
    ) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.userReader = userReader;
        this.kakaoGeocodeClient = kakaoGeocodeClient;
        this.pageStateRepo = pageStateRepo;
        this.adminEmail = adminEmail;
        this.defaultParkingLotImageUrl = defaultParkingLotImageUrl;

    }

    public void savePublicData(List<ParkingLotData> dataList) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setAutoCommit(false);

            // 현재 페이지 가져오기
            int page = pageStateRepo.readPage(connection);

            // dto -> entity 변환
            List<ParkingLot> parkingLots = dataList.stream()
                    .map(this::convertToParkingLot)
                    .toList();

            // 가져온 데이터 저장
            bulkInsertParkingLots(connection, parkingLots);
            bulkInsertImages(connection, parkingLots);

            // 페이지 업데이트
            int nextPage = dataList.isEmpty() ? 1 : page + 1;
            pageStateRepo.updatePage(connection, nextPage);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("DB 저장 실패", e);
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
