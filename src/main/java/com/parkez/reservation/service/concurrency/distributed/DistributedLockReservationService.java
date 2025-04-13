package com.parkez.reservation.service.concurrency.distributed;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.reservation.service.ReservationWriter;
import com.parkez.reservation.service.concurrency.ReservationLockService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service("distributed")
@RequiredArgsConstructor
public class DistributedLockReservationService implements ReservationLockService {

    private final DistributedLockManager distributedLockManager;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final ReservationWriter reservationWriter;

    @Override
    public MyReservationResponse createReservation(AuthUser authUser, ReservationRequest request) {

        return distributedLockManager.executeWithLock(request.getParkingZoneId(), () -> {

            User user = userReader.getActiveById(authUser.getId());
            ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(request.getParkingZoneId());

            // 요금 계산
            long hours = ChronoUnit.HOURS.between(request.getStartDateTime(), request.getEndDateTime());
            if (hours <= 0) {
                throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
            }
            BigDecimal price = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

            Reservation reservation = reservationWriter.createReservation(
                    user,
                    parkingZone,
                    parkingZone.getParkingLotName(),
                    request.getStartDateTime(),
                    request.getEndDateTime(),
                    price
            );

            boolean reviewWritten = false;

            return MyReservationResponse.of(reservation, reviewWritten);
        });
    }
}
