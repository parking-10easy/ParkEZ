package com.parkez.parkinglot.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.service.ParkingLotPublicDataService;
import com.parkez.user.service.JdbcUserReader;

import java.io.IOException;
import java.net.URISyntaxException;

public class PublicDataHandler
        implements RequestHandler<ScheduledEvent, Void> {

    private final ParkingLotPublicDataService parkingLotPublicDataService;

    public PublicDataHandler() {
        String dataUrl = System.getenv("PARKING_LOT_PUBLIC_DATA_URL");
        String serviceKey = System.getenv("PARKING_LOT_PUBLIC_DATA_SERVICE_KEY");
        String defaultImg = System.getenv("PARKING_LOT_DEFAULT_IMAGE_URL");
        String adminEmail = System.getenv("PARKING_LOT_PUBLIC_DATA_ADMIN_EMAIL");
        String jdbcUrl = System.getenv("JDBC_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String kakaoKey = System.getenv("KAKAO_API_KEY");
        SimpleKakaoGeocodeClient geocodeClient = new SimpleKakaoGeocodeClient(kakaoKey);
        JdbcUserReader userReader = new JdbcUserReader(jdbcUrl, dbUser, dbPassword);

        this.parkingLotPublicDataService = new ParkingLotPublicDataService(
                dataUrl,
                serviceKey,
                defaultImg,
                adminEmail,
                jdbcUrl,
                dbUser,
                dbPassword,
                geocodeClient,
                userReader
        );
    }

    @Override
    public Void handleRequest(ScheduledEvent scheduledEvent, Context context) {
        try {
            parkingLotPublicDataService.fetchAndSavePublicData();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
