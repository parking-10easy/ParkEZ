package com.parkez.reservation.service;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationReader {

    private final ReservationRepository reservationRepository;

    public Page<Reservation> findMyReservations(Long userId, PageRequest pageable) {

       return reservationRepository.findByUserId(userId, pageable);
    }
}
