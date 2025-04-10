package com.parkez.parkingLot;


import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.service.ParkingLotWriter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingLotWriterTest {

    @InjectMocks
    private ParkingLotWriter parkingLotWriter;

    @Mock
    private ParkingLotRepository parkingLotrepository;

    private ParkingLot getParkingLot() {
        return ParkingLot.builder()
                .name("테스트 주차장")
                .build();
    }

    @Nested
    class createParkingLot {
        @Test
        void ParkingLot을_Repository를_통해_save한다() {
            // given
            ParkingLot parkingLot = getParkingLot();
            when(parkingLotrepository.save(any(ParkingLot.class))).thenReturn(parkingLot);

            // when
            ParkingLot result = parkingLotWriter.createParkingLot(parkingLot);

            //then
            assertNotNull(result);
            assertEquals(getParkingLot().getName(), result.getName());
            verify(parkingLotrepository).save(parkingLot);
        }
    }

    @Nested
    class deleteParkingLot {

        @Test
        void ParkingLot의_deletedAt을_추가한다() {
            // given
            ParkingLot parkingLot = getParkingLot();
            assertNull(parkingLot.getDeletedAt());

            // when
            parkingLotWriter.deleteParkingLot(parkingLot);

            // then
            assertNotNull(parkingLot.getDeletedAt());
        }
    }
}
