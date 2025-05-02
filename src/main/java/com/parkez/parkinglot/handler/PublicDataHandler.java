package com.parkez.parkinglot.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.domain.repository.PageStateRepository;
import com.parkez.parkinglot.domain.repository.PageStateRepositoryImpl;
import com.parkez.parkinglot.service.PublicDataReader;
import com.parkez.parkinglot.service.PublicDataWriter;
import com.parkez.user.service.JdbcUserReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class PublicDataHandler
        implements RequestHandler<ScheduledEvent, Void> {


    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    private final PublicDataReader reader;
    private final PublicDataWriter writer;
    private final PageStateRepository pageStateRepository;


    public PublicDataHandler() {
        this.jdbcUrl = System.getenv("JDBC_URL");
        this.dbUser = System.getenv("DB_USERNAME");
        this.dbPassword = System.getenv("DB_PASSWORD");
        String dataUrl = System.getenv("PARKING_LOT_PUBLIC_DATA_URL");
        String serviceKey = System.getenv("PARKING_LOT_PUBLIC_DATA_SERVICE_KEY");
        String defaultImg = System.getenv("PARKING_LOT_DEFAULT_IMAGE_URL");
        String adminEmail = System.getenv("PARKING_LOT_PUBLIC_DATA_ADMIN_EMAIL");
        String kakaoKey = System.getenv("KAKAO_API_KEY");

        this.reader = new PublicDataReader(dataUrl, serviceKey);
        this.pageStateRepository = new PageStateRepositoryImpl();

        JdbcUserReader userReader = new JdbcUserReader(jdbcUrl, dbUser, dbPassword);
        SimpleKakaoGeocodeClient geocodeClient = new SimpleKakaoGeocodeClient(kakaoKey);

        this.writer = new PublicDataWriter(
                jdbcUrl, dbUser, dbPassword,
                userReader, geocodeClient,
                pageStateRepository, adminEmail,
                defaultImg

        );
    }

    @Override
    public Void handleRequest(ScheduledEvent scheduledEvent, Context context) {
        int page;
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setAutoCommit(false);
            page = pageStateRepository.readPage(connection);
        } catch (SQLException e) {
            throw new RuntimeException("페이지 상태 읽기 실패", e);
        }
        List<ParkingLotData> dataList = reader.fetchPage(page);
        writer.savePublicData(dataList);
        return null;
    }
}
