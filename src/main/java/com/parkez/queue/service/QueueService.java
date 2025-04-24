package com.parkez.queue.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.domain.repository.QueueRepository;
import com.parkez.queue.dto.WaitingUserDto;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.queue.redis.QueueKey;
import com.parkez.reservation.dto.request.ReservationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QueueRepository queueRepository;

    private static final String QUEUE_KEY_PREFIX = "reservation:queue:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final long CANCEL_LIMIT_HOURS = 1L;

    // 예약 대기열 등록 로직
    public JoinQueueResult joinWaitingQueue(Long userId, ReservationRequest request){

        // 대기열 키 생성
        String key = QueueKey.generateKey(
                request.getParkingZoneId(),
                request.getStartDateTime(),
                request.getEndDateTime()
        );

        if (queueRepository.isAlreadyInQueue(key, userId)) {
            return JoinQueueResult.ALREADY_JOINED;
        }

        // 대기자 DTO 생성
        WaitingUserDto dto = new WaitingUserDto(
                userId,
                request.getParkingZoneId(),
                request.getStartDateTime(),
                request.getEndDateTime()
        );

        // 대기열에 추가
        queueRepository.enqueue(key, dto);

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
                        LocalDateTime.parse(map.get("startDateTime").toString()),
                        LocalDateTime.parse(map.get("endDateTime").toString())
                );
            } catch (Exception e) {
                log.warn("[대기열] DTO 변환 중 오류", e);
                throw new ParkingEasyException(QueueErrorCode.DTO); //todo
            }
        }

        throw new ParkingEasyException(QueueErrorCode.DTO2);
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
                        log.info("[알림] userId={} 대기열 만료 안내 메일 전송 (예정)", dto.getUserId());
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

}
