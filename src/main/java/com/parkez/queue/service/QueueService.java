package com.parkez.queue.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.domain.repository.QueueRepository;
import com.parkez.queue.dto.WaitingUserDto;
import com.parkez.queue.dto.response.MyWaitingQueueDetailResponse;
import com.parkez.queue.dto.response.MyWaitingQueueListResponse;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.queue.redis.QueueKey;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QueueRepository queueRepository;
    private final ReservationReader reservationReader;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;

    private static final String QUEUE_KEY_PREFIX = "reservation:queue:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final long CANCEL_LIMIT_HOURS = 1L;

    // 예약 대기열 등록 로직
    public JoinQueueResult joinWaitingQueue(Long userId, ReservationRequest request) {
        String queueKey = generateQueueKey(request.getParkingZoneId(), request.getStartDateTime(), request.getEndDateTime());

        if (queueRepository.isAlreadyInQueue(queueKey, userId)) {
            return JoinQueueResult.ALREADY_JOINED;
        }

        WaitingUserDto dto = new WaitingUserDto(userId, request.getParkingZoneId(), request.getStartDateTime(), request.getEndDateTime());
        queueRepository.enqueue(queueKey, dto);

        return JoinQueueResult.JOINED;
    }

    // 예약 취소 시 → 대기열에서 다음 사용자 꺼내 waitingUserDto로 변환
    public WaitingUserDto dequeueConvertToDto(String key) {
        Object obj = queueRepository.dequeue(key);
        log.info("[대기열] dequeue 결과: {}", obj);
        return convertToDto(obj);
    }

    private WaitingUserDto convertToDto(Object obj) {
        if (obj instanceof Map map) {
            try {
                return new WaitingUserDto(
                        Long.valueOf(map.get("userId").toString()),
                        Long.valueOf(map.get("parkingZoneId").toString()),
                        LocalDateTime.parse(map.get("reservationStartDateTime").toString()),
                        LocalDateTime.parse(map.get("reservationEndDateTime").toString())
                );
            } catch (NumberFormatException e) {
                log.warn("[대기열] DTO 변환 중 숫자 변환 오류", e);
                throw new ParkingEasyException(QueueErrorCode.DTO_CONVERT_FAIL_NUMBER);
            } catch (DateTimeParseException e) {
                log.warn("[대기열] DTO 변환 중 날짜 변환 오류", e);
                throw new ParkingEasyException(QueueErrorCode.DTO_CONVERT_FAIL_TIME);
            } catch (NullPointerException e) {
                log.warn("[대기열] DTO 변환 중 Null 값 오류", e);
                throw new ParkingEasyException(QueueErrorCode.DTO_CONVERT_FAIL_NULL);
            }
        }
        throw new ParkingEasyException(QueueErrorCode.DTO_CONVERT_FAIL_TYPE);
    }

    public void deleteExpiredQueues() {
        Set<String> queueKeys = redisTemplate.keys(QUEUE_KEY_PREFIX + "*");

        if (queueKeys.isEmpty()) return;

        for (String queueKey : queueKeys) {
            try {
                LocalDateTime startTime = extractStartTimeFromKey(queueKey);

                if (isQueueExpired(startTime)) {
                    log.info("[대기열 만료] queueKey={} → 1시간 전 도달, 대기열 삭제 진행", queueKey);

                    List<Object> waitingList = queueRepository.getAll(queueKey);
                    queueRepository.deleteQueue(queueKey);

                    for (Object obj : waitingList) {
                        WaitingUserDto dto = convertToDto(obj);
                        log.info("[알림] userId={} 대기열 만료 안내 메일 전송 (예정)", dto.getUserId()); //todo
                    }
                }
            } catch (Exception e) {
                log.error("[대기열 스케줄러] 키 파싱 실패: key={}", queueKey, e);
            }
        }
    }

    private LocalDateTime extractStartTimeFromKey(String key) {
        String[] parts = key.split(":");
        String[] timeRange = parts[3].split("-");
        return LocalDateTime.parse(timeRange[0], FORMATTER);
    }

    private boolean isQueueExpired(LocalDateTime startTime) {
        return startTime.minusHours(CANCEL_LIMIT_HOURS).isBefore(LocalDateTime.now());
    }

    public List<MyWaitingQueueListResponse> findMyWaitingQueues(AuthUser authUser) {
        Set<String> queueKeys = queueRepository.findAllQueueKeys();

        if (queueKeys == null || queueKeys.isEmpty()) {
            return List.of();
        }

        List<MyWaitingQueueListResponse> result = new ArrayList<>();

        for (String queueKey : queueKeys) {
            List<Object> waitingList = queueRepository.getWaitingList(queueKey);
            if (waitingList == null || waitingList.isEmpty()) continue;

            for (int i = 0; i < waitingList.size(); i++) {
                WaitingUserDto dto = convertToDto(waitingList.get(i));

                if (isMine(dto, authUser)) {
                    Reservation reservation = reservationReader.findReservationByQueueKey(
                            dto.getParkingZoneId(),
                            dto.getReservationStartDateTime(),
                            dto.getReservationEndDateTime()
                    );
                    ParkingZone zone = parkingZoneReader.getActiveByParkingZoneId(dto.getParkingZoneId());

                    result.add(MyWaitingQueueListResponse.builder()
                            .reservationId(reservation.getId())
                            .parkingZoneId(zone.getId())
                            .parkingZoneName(zone.getName())
                            .startDateTime(dto.getReservationStartDateTime())
                            .endDateTime(dto.getReservationEndDateTime())
                            .myQueue(i + 1)
                            .build()
                    );
                    break;
                }
            }
        }
        return result;
    }

    public MyWaitingQueueDetailResponse findMyQueue(AuthUser authUser, Long reservationId) {
        Reservation reservation = reservationReader.findById(reservationId);

        String queueKey = generateQueueKey(reservation.getParkingZoneId(), reservation.getStartDateTime(), reservation.getEndDateTime());

        List<Object> waitingList = queueRepository.getWaitingList(queueKey);
        if (waitingList == null || waitingList.isEmpty()) {
            throw new ParkingEasyException(QueueErrorCode.NOT_IN_QUEUE);
        }

        OptionalInt myPositionOpt = findMyPositionInQueue(waitingList, authUser);

        int myPosition = myPositionOpt.orElseThrow(() -> new ParkingEasyException(QueueErrorCode.NOT_IN_QUEUE)) + 1;
        ParkingZone zone = parkingZoneReader.getActiveByParkingZoneId(reservation.getParkingZone().getId());

        return MyWaitingQueueDetailResponse.builder()
                .parkingZoneId(zone.getId())
                .parkingZoneName(zone.getName())
                .startDateTime(reservation.getStartDateTime())
                .endDateTime(reservation.getEndDateTime())
                .myQueue(myPosition)
                .build();
    }

    public void cancelMyQueue(AuthUser authUser, Long reservationId) {
        Reservation reservation = reservationReader.findById(reservationId);

        String queueKey = generateQueueKey(reservation.getParkingZoneId(), reservation.getStartDateTime(), reservation.getEndDateTime());

        List<Object> waitingList = queueRepository.getWaitingList(queueKey);
        if (waitingList == null || waitingList.isEmpty()) {
            throw new ParkingEasyException(QueueErrorCode.NOT_IN_QUEUE);
        }

        Object myWaiting = waitingList.stream()
                .filter(obj -> isMine(convertToDto(obj), authUser))
                .findFirst()
                .orElseThrow(() -> new ParkingEasyException(QueueErrorCode.NOT_IN_QUEUE));

        queueRepository.removeFromQueue(queueKey, myWaiting);
        log.info("[대기열] userId={} 대기열 취소 완료", authUser.getId());
    }

    private String generateQueueKey(Long parkingZoneId, LocalDateTime start, LocalDateTime end) {
        return QueueKey.generateKey(parkingZoneId, start, end);
    }

    private boolean isMine(WaitingUserDto dto, AuthUser authUser) {
        return dto.getUserId() != null && dto.getUserId().equals(authUser.getId());
    }

    private OptionalInt findMyPositionInQueue(List<Object> waitingList, AuthUser authUser) {
        return IntStream.range(0, waitingList.size())
                .filter(i -> isMine(convertToDto(waitingList.get(i)), authUser))
                .findFirst();
    }
}
