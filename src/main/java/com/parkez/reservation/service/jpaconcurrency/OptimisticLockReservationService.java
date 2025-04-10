package com.parkez.reservation.service.jpaconcurrency;

import com.parkez.common.principal.AuthUser;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;
import org.springframework.stereotype.Service;

@Service("optimistic")
public class OptimisticLockReservationService implements ReservationLockService {

    @Override
    public MyReservationResponse createReservation(AuthUser authUser, ReservationRequest request) {
        return null;
    }
}
