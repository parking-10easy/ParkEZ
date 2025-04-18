package com.parkez.parkinglot.scheduler;

import com.parkez.parkinglot.service.ParkingLotPublicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParkingLotScheduler {

    private final ParkingLotPublicDataService parkingLotPublicDataService;

    @Scheduled(cron = "0 0 2 * * *")
    public void getPublicData() {
        parkingLotPublicDataService.fetchAndSavePublicData();
    }
}
