package com.parkez.reservation.service.concurrency.distributed;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.reservation.distributedlockmanager.DistributedLockManager;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.reservation.service.ReservationWriter;
import com.parkez.reservation.service.concurrency.ReservationLockService;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("distributedLockService")
@RequiredArgsConstructor
public class DistributedLockReservationService implements ReservationLockService {

    private final DistributedLockManager distributedLockManager;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final ReservationWriter reservationWriter;
    private final ReservationReader reservationReader;

    @Override
    public MyReservationResponse createReservation(AuthUser authUser, ReservationRequest request) {

        return distributedLockManager.executeWithLock(request.getParkingZoneId(), () -> {

            User user = userReader.getActiveUserById(authUser.getId());
            ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(request.getParkingZoneId());

            // 예약 날짜 및 시간 입력 오류 예외
            if (request.getStartDateTime().isAfter(request.getEndDateTime())) {
                throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
            }

            // 이미 해당 시간에 예약이 존재할 경우
            List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
            boolean existed = reservationReader.existsReservationByConditions(parkingZone, request.getStartDateTime(), request.getEndDateTime(), statusList);
            if (existed) {
                throw new ParkingEasyException(ReservationErrorCode.ALREADY_RESERVED);
            }

            Reservation reservation = reservationWriter.create(
                    user,
                    parkingZone,
                    request.getStartDateTime(),
                    request.getEndDateTime()
            );

            boolean reviewWritten = false;

            return MyReservationResponse.of(reservation, reviewWritten);
        });
    }
}
