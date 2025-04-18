package com.parkez.reservation.service;

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
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;
import com.parkez.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class ReservationServiceConcurrencyTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParkingLotRepository parkingLotRepository;
    @Autowired
    private ParkingZoneRepository parkingZoneRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;

    private static final LocalTime OPENED_AT = LocalTime.of(9, 0, 0);
    private static final LocalTime CLOSED_AT = LocalTime.of(21, 0, 0);
    private static final LocalDateTime RESERVATION_START_DATE_TIME = LocalDateTime.of(LocalDate.now().plusDays(1), OPENED_AT);
    private static final LocalDateTime RESERVATION_END_DATE_TIME = LocalDateTime.of(LocalDate.now().plusDays(1), CLOSED_AT);

    private User user;
    private ParkingZone parkingZone;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {

        user = userRepository.save(User.builder()
                .email("test@example.com")
                .password("Qwer123!")
                .nickname("test")
                .phone("010-1234-5678")
                .role(UserRole.ROLE_OWNER)
                .loginType(LoginType.NORMAL)
                .status(UserStatus.COMPLETED)
                .build());

        authUser = AuthUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(user.getRoleName())
                .nickname(user.getNickname())
                .build();

        ParkingLot parkingLot = parkingLotRepository.save(ParkingLot.builder()
                .owner(user)
                .name("테스트 주차장")
                .address("서울시 강남구")
                .pricePerHour(BigDecimal.valueOf(2000))
                .openedAt(OPENED_AT)
                .closedAt(CLOSED_AT)
                .description("설명")
                .quantity(10)
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.OWNER_REGISTERED)
                .build());

        parkingZone = parkingZoneRepository.save(ParkingZone.builder()
                .parkingLot(parkingLot)
                .name("A구역")
                .build());
    }

    private ReservationRequest createRequest() {

        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "parkingZoneId", parkingZone.getId());
        ReflectionTestUtils.setField(request, "startDateTime", RESERVATION_START_DATE_TIME);
        ReflectionTestUtils.setField(request, "endDateTime", RESERVATION_END_DATE_TIME);

        return request;
    }

    @Test
    void 동시_요청에도_오직_하나의_예약만_성공해야_한다() throws InterruptedException {

        // given
        int requestCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        ReservationRequest request = createRequest();

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.createReservation(authUser, request);
                    successCount.incrementAndGet();
                } catch (ParkingEasyException e) {
                    log.warn("실패한 요청: {}", e.getErrorCode());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        List<Reservation> result = reservationRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        log.info("성공 요청 수: {}", successCount.get());
        log.info("실패 요청 수: {}", failCount.get());
    }
}