package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingLotReaderTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @InjectMocks
    private ParkingLotReader parkingLotReader;

    private User getOwner() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("테스트 소유자")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;
    }

    private User getOwner2() {
        User owner2 = User.builder()
                .email("owner2@test.com")
                .nickname("테스트 소유자2")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner2, "id", 2L);
        return owner2;
    }

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(getOwner())
                .name("Main Parking Lot")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    @Nested
    class GetOwnedParkingLot {
        @Test
        void 특정_주차장을_조회할_수_있다() {
            // given
            Long userId = 1L;
            Long parkingLotId = 1L;
            ParkingLot parkingLot = getParkingLot();

            when(parkingLotRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.of(parkingLot));

            // when
            ParkingLot result = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);

            // then
            assertThat(result).extracting("id","name")
                    .containsExactly(1L,"Main Parking Lot");
        }

        @Test
        void 특정_주차장이_존재하지_않으면_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long invalidParkingLotId = -1L;
            when(parkingLotRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> parkingLotReader.getOwnedParkingLot(userId, invalidParkingLotId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 특정_주차장의_소유자_본인이_아니면_NOT_PARKING_LOT_OWNER_예외가_발생한다() {
            // given
            User nonOwner = getOwner2();
            ParkingLot parkingLot = mock(ParkingLot.class);

            when(parkingLotRepository.findByIdAndDeletedAtIsNull(anyLong()))
                    .thenReturn(Optional.of(parkingLot));
            when(parkingLot.isOwned(anyLong())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingLotReader.getOwnedParkingLot(nonOwner.getId(), parkingLot.getId()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER.getDefaultMessage());
        }
    }

    @Nested
    class ValidateExistence {
        @Test
        void 특정_주차장이_존재하면_예외가_발생하지_않는다() {
            // given
            Long validParkingLotId = 1L;
            when(parkingLotRepository.existsByIdAndDeletedAtIsNull(anyLong())).thenReturn(true);

            // when & then
            assertThatCode(() -> parkingLotReader.validateExistence(validParkingLotId))
                    .doesNotThrowAnyException();
        }

        @Test
        void 특정_주차장이_존재하지_않으면_NOT_FOUND_예외가_발생한다() {
            // given
            Long invalidParkingLotId = -1L;
            when(parkingLotRepository.existsByIdAndDeletedAtIsNull(anyLong())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> parkingLotReader.validateExistence(invalidParkingLotId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }
    }
}