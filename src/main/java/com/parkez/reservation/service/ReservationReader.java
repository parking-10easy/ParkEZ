package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import com.parkez.reservation.exception.ReservationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationReader {

    private final ReservationRepository reservationRepository;

    public Page<ReservationWithReviewDto> findMyReservations(Long userId, PageRequest pageable) {

        // 예약 내역이 없을 경우 빈 페이지 전달
        if (!reservationRepository.existsByUser_Id(userId)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return reservationRepository.findAllWithReviewByUser_Id(userId, pageable);
    }

    public Reservation findMyReservation(Long userId, Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new ParkingEasyException(ReservationErrorCode.NOT_FOUND_RESERVATION)
        );

        // 본인이 한 예약이 아닐 경우 예외
        if (!reservation.isOwnedBy(userId)) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_MY_RESERVATION);
        }

        return reservation;
    }

    public Page<Reservation> findOwnerReservations(Long parkingZoneId, PageRequest pageable) {

        // 예약 내역이 없을 경우 빈 페이지 전달
        if (!reservationRepository.existsByParkingZone_Id(parkingZoneId)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        return reservationRepository.findAllByParkingZone_Id(parkingZoneId, pageable);
    }

    public boolean existsReservationByConditions(ParkingZone parkingZone, LocalDateTime startDateTime, LocalDateTime endDateTime, List<ReservationStatus> statusList) {
        return reservationRepository.existsReservationByConditions(parkingZone, startDateTime, endDateTime, statusList);
    }

    public boolean existsActiveReservationByParkingZoneId(Long parkingZoneId) {
        return reservationRepository.existsActiveReservationByParkingZoneId(parkingZoneId);
    }

    /* Alarm 도메인에서 필요한 메서드 - 예약 만료 10분 전 알림 대상 */
    public List<Reservation> findUpcomingReservations(LocalDateTime now, LocalDateTime tenMinutesLater) {
        return reservationRepository.findConfirmedReservationsBetween(ReservationStatus.CONFIRMED, now, tenMinutesLater);
    }

    /* Alarm 도메인에서 필요한 메서드 - 예약 만료 알림 대상 */
    public List<Reservation> findExpiredReservations(LocalDateTime now) {
        return reservationRepository.findExpiredReservations(ReservationStatus.CONFIRMED, now);
    }
}
