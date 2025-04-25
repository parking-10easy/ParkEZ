package com.parkez.reservation.service;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.user.domain.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationWriter {

	private final ReservationRepository reservationRepository;

	public Reservation create(
		User user, ParkingZone parkingZone, LocalDateTime startDateTime, LocalDateTime endDateTime,
		BigDecimal originalPrice, BigDecimal discountAmount, BigDecimal finalPrice, Long promotionIssueId
	) {
		Reservation reservation = Reservation.builder()
			.user(user)
			.parkingZone(parkingZone)
			.parkingLotName(parkingZone.getParkingLotName())
			.startDateTime(startDateTime)
			.endDateTime(endDateTime)
			.price(finalPrice)
			.originalPrice(originalPrice)
			.discountAmount(discountAmount)
			.promotionIssueId(promotionIssueId)
			.build();

		return reservationRepository.save(reservation);
	}

	public void complete(Reservation reservation) {
		reservation.complete(LocalDateTime.now());
	}

	public void cancel(Reservation reservation) {
		reservation.cancel();
	}

	public void updateStatusConfirm(Reservation reservation) {
		reservation.confirm();
	}

	public void expirePaymentTimeout(Reservation reservation) {
		reservation.expire();
	}

	public void expire(LocalDateTime expiredTime) {
		List<Reservation> expireToReservation = reservationRepository.findReservationsToExpire(expiredTime);

		if (!expireToReservation.isEmpty()) {
			expireToReservation.forEach(Reservation::expire);
			reservationRepository.saveAll(expireToReservation);
		}
	}

}
