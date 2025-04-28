package com.parkez.parkinglot.service;

import com.parkez.parkinglot.client.kakaomap.geocode.SimpleKakaoGeocodeClient;
import com.parkez.parkinglot.client.publicData.ParkingLotData;
import com.parkez.parkinglot.domain.repository.PageStateRepositoryImpl;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.JdbcUserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PublicDataWriterTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    String jdbcUrl;
    @Value("${spring.datasource.username}")
    String dbUser;
    @Value("${spring.datasource.password}")
    String dbPassword;

    PublicDataWriter writer;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        jdbcTemplate.execute("DELETE FROM parking_lot_image");
        jdbcTemplate.execute("DELETE FROM parking_lot");
        jdbcTemplate.execute("DELETE FROM public_data_page_state");
        jdbcTemplate.execute("DELETE FROM users");

        jdbcTemplate.update(
                """
                        INSERT INTO users
                          (id, email, password, nickname, role, login_type, status,
                           deleted_at, created_at, modified_at)
                        VALUES
                          (1, ?, ?, ?, ?, ?, ?, NULL, NOW(), NOW())
                        """,
                "admin@parkez.com",
                "dummyPassword123!",
                "admin",
                "ROLE_ADMIN",
                "KAKAO",
                "COMPLETED"
        );

        // 초기 페이지 상태 삽입
        jdbcTemplate.update(
                "INSERT INTO public_data_page_state(id, current_page, updated_at) " +
                        "VALUES (1, ?, NOW())",
                5
        );

        JdbcUserReader userReader = new JdbcUserReader(jdbcUrl, dbUser, dbPassword) {
            @Override
            public User getUserByEmailAndRole(String email, UserRole role) {
                return User.ofIdEmailRole(1L, email, role);
            }
        };

        SimpleKakaoGeocodeClient geoClient = new SimpleKakaoGeocodeClient("DUMMY_KEY");
        writer = new PublicDataWriter(
                jdbcUrl, dbUser, dbPassword,
                userReader, geoClient,
                new PageStateRepositoryImpl(),
                "admin@parkez.com",
                "parking-lot-default.jpg"
        );
    }

    @Nested
    class savePublicData {

        @Test
        void 데이터가_없을_때_nextPage가_1이_된다() {
            // given
            List<ParkingLotData> dataList = Collections.emptyList();
            writer.savePublicData(dataList);

            // then
            Integer page = jdbcTemplate.queryForObject(
                    "SELECT current_page FROM public_data_page_state WHERE id=1",
                    Integer.class
            );
            assertThat(page).isEqualTo(1);
        }

        @Test
        void 데이터가_있으면_DB에_저장되고_nextPage가_증가한다() {
            //given
            ParkingLotData sample = ParkingLotData.builder()
                    .name("TestLot")
                    .address("123 Main St")
                    .latitude("37.0")
                    .longitude("127.0")
                    .quantity("10")
                    .openedAt("08:00")
                    .closedAt("22:00")
                    .chargeType("유료")
                    .build();

            // when
            writer.savePublicData(Collections.singletonList(sample));

            // then
            Integer lots = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parking_lot", Integer.class);
            Integer imgs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parking_lot_image", Integer.class);
            assertThat(lots).isEqualTo(1);
            assertThat(imgs).isEqualTo(1);

            Integer page = jdbcTemplate.queryForObject(
                    "SELECT current_page FROM public_data_page_state WHERE id=1",
                    Integer.class
            );
            assertThat(page).isEqualTo(6);
        }
    }
}