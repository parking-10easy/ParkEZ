package com.parkez.reservation.service.concurrency.pessimistic;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class PessimisticLockReservationConcurrencyTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParkingLotRepository parkingLotRepository;
    @Autowired
    private ParkingZoneRepository parkingZoneRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PessimisticLockReservationService pessimisticLockReservationService;

    private User createUser() {
        return User.builder()
                .email("test@example.com")
                .password("Qwer123!")
                .nickname("user")
                .phone("010-1234-5678")
                .build();
    }

    private ParkingLot createParkingLot(User owner) {
        return ParkingLot.builder()
                .owner(owner)
                .name("테스트 주차장")
                .address("서울시 강남구")
                .pricePerHour(BigDecimal.valueOf(2000))
                .openedAt(LocalTime.of(9, 0))
                .closedAt(LocalTime.of(23, 0))
                .description("설명")
                .quantity(10)
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.OWNER_REGISTERED)
                .build();
    }

    private ParkingZone createParkingZone(ParkingLot lot) {
        return ParkingZone.builder()
                .parkingLot(lot)
                .name("A구역")
                .build();
    }

    private ReservationRequest createRequest(Long parkingZoneId, LocalDateTime start, LocalDateTime end) {
        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", parkingZoneId);
        ReflectionTestUtils.setField(request, "startDateTime", start);
        ReflectionTestUtils.setField(request, "endDateTime", end);
        return request;
    }

    @Test
    void 서로_다른_사용자가_동시에_예약을_시도하면_단_하나만_성공한다() throws InterruptedException {
        // given
        User user = userRepository.save(createUser());
        AuthUser authUser = AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(UserRole.Authority.USER)
                .nickname(user.getNickname())
                .build();

        ParkingLot parkingLot = parkingLotRepository.save(createParkingLot(user));
        ParkingZone parkingZone = parkingZoneRepository.save(createParkingZone(parkingLot));

        LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = start.plusHours(1);

        ReservationRequest request = createRequest(parkingZone.getId(), start, end);

        int requestCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(requestCount);

        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    pessimisticLockReservationService.createReservation(authUser, request);
                    log.info("[성공] 예약 요청 성공");
                } catch (ParkingEasyException e) {
                    if (e.getErrorCode().equals(ReservationErrorCode.ALREADY_RESERVED)) {
                        log.warn(e.getMessage());
                    } else {
                        log.error("예상치 못한 예외 발생", e);
                    }
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        List<Reservation> result = reservationRepository.findAll();
        assertThat(result).hasSize(1);
        log.info("성공 요청 수: {}", result.size());
        log.info("실패 요청 수: {}", failCount.get());
    }
}