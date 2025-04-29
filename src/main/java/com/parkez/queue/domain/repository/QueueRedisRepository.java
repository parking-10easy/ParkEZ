package com.parkez.queue.domain.repository;

import com.parkez.queue.dto.WaitingUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // 대기열에 사용자 추가
    public void enqueue(String key, WaitingUserDto waitingUserDto) {
        redisTemplate.opsForList().rightPush(key, waitingUserDto);
    }

    //대기열에서 사용자 꺼내기 (FIFO)
    public Object dequeue(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // 키에 존재하는 대기열 모두 조회
    public List<Object> getAll(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    //대기열 삭제
    public void deleteQueue(String key) {
        redisTemplate.delete(key);
    }

    // 예약 대기열 중복 확인 로직
    public boolean isAlreadyInQueue(String key, Long userId) {
        List<Object> waitingList = getAll(key);

        return waitingList.stream()
                .anyMatch(obj -> {
                    if (obj instanceof Map map) {
                        Object id = map.get("userId");
                        return id != null && id.toString().equals(userId.toString());
                    }
                    return false;
                });

    }

    public Set<String> findAllQueueKeys() {
        return redisTemplate.keys("reservation:queue:*");
    }

    public List<Object> getWaitingList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void removeFromQueue(String key, Object target) {
        redisTemplate.opsForList().remove(key, 1, target);
    }

}
