package com.parkez.reservation.service;

import com.parkez.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationReader {

    private final ReservationRepository reservationRepository;
}
