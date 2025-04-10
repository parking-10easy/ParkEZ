package com.parkez.reservation.service.jpaconcurrency;

import com.parkez.common.principal.AuthUser;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.MyReservationResponse;

public interface ReservationLockService {

    MyReservationResponse createReservation(AuthUser authUser, ReservationRequest request);
}
