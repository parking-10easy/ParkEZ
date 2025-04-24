package com.parkez.settlement.scheduler;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.service.PaymentReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;
import com.parkez.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class SettlementJobTest {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobRegistry jobRegistry;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SettlementDetailRepository settlementDetailRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ParkingLotRepository parkingLotRepository;
    @Autowired
    private ParkingZoneRepository parkingZoneRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentReader paymentReader;
    @Autowired
    private DataSource dataSource;

    private static final LocalTime OPENED_AT = LocalTime.of(9, 0, 0);
    private static final LocalTime CLOSED_AT = LocalTime.of(21, 0, 0);
    private static final LocalDateTime RESERVATION_START_DATE_TIME = LocalDateTime.of(LocalDate.of(2025,3,10), OPENED_AT);
    private static final LocalDateTime RESERVATION_END_DATE_TIME = LocalDateTime.of(LocalDate.of(2025,3,10), CLOSED_AT);

    private User user;
    private User owner;
    private ParkingLot parkingLot;
    private ParkingZone parkingZone;
    private Reservation reservation;
    private Payment payment;
    private BigDecimal price = BigDecimal.valueOf(10000);

    @BeforeEach
    void clearBatchMetadata() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("TRUNCATE TABLE BATCH_STEP_EXECUTION_CONTEXT");
            stmt.execute("TRUNCATE TABLE BATCH_JOB_EXECUTION_CONTEXT");
            stmt.execute("TRUNCATE TABLE BATCH_STEP_EXECUTION");
            stmt.execute("TRUNCATE TABLE BATCH_JOB_EXECUTION_PARAMS");
            stmt.execute("TRUNCATE TABLE BATCH_JOB_EXECUTION");
            stmt.execute("TRUNCATE TABLE BATCH_JOB_INSTANCE");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    @BeforeEach
    void setUp() {

        user = userRepository.save(User.builder()
                .email("SettlementTest@example.com")
                .password("Qwer123!")
                .nickname("test")
                .phone("010-1234-5678")
                .role(UserRole.ROLE_USER)
                .loginType(LoginType.NORMAL)
                .status(UserStatus.COMPLETED)
                .build());

        owner = userRepository.save(User.builder()
                .email("SettlementTest@example.com")
                .password("Qwer123!")
                .nickname("test")
                .phone("010-1234-5678")
                .role(UserRole.ROLE_OWNER)
                .loginType(LoginType.NORMAL)
                .status(UserStatus.COMPLETED)
                .build());

        parkingLot = parkingLotRepository.save(ParkingLot.builder()
                .owner(owner)
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

        reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLot.getName())
                .startDateTime(RESERVATION_START_DATE_TIME)
                .endDateTime(RESERVATION_END_DATE_TIME)
                .price(price)
                .build());
        reservation.complete(RESERVATION_END_DATE_TIME);
        reservationRepository.saveAndFlush(reservation);

        payment = paymentRepository.save(Payment.builder()
                .user(user)
                .reservation(reservation)
                .paymentStatus(PaymentStatus.APPROVED)
                .orderId("orderId")
                .build());
    }

    @Test
    void 스프링_배치를_이용한_정산_테스트() throws Exception {
        // given
        LocalDateTime runtime = LocalDateTime.of(2025, 4, 1, 0, 0);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("runtime", runtime.toString())
                .toJobParameters();

        // when
        log.info("settlement batch start");
        JobExecution execution = jobLauncher.run(jobRegistry.getJob("settlementJob"), jobParameters);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 정산 결과 확인
        List<Settlement> settlements = settlementRepository.findAll();
        Settlement settlement = settlements.get(0);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);

        List<SettlementDetail> details = settlementDetailRepository.findAll();
        SettlementDetail settlementDetail = details.get(0);
        assertThat(settlementDetail.getReservation().getId()).isEqualTo(reservation.getId());
    }
}