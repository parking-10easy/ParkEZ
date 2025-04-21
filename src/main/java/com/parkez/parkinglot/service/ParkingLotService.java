package com.parkez.parkinglot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.client.kakaomap.geocode.Geocode;
import com.parkez.parkinglot.client.kakaomap.geocode.KakaoGeocodeClient;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.rediscache.ParkingLotSearchRedisKey;
import com.parkez.parkinglot.rediscache.RestPage;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotWriter parkingLotWriter;
    private final ParkingLotReader parkingLotReader;
    private final UserReader userReader;
    private final KakaoGeocodeClient kakaoGeocodeClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    private static final long CACHE_TTL = 5L;

    @Value("${parking-lot.default-image-url}")
    private String defaultParkingLotImageUrl;

    // 주차장 생성
    public ParkingLotResponse createParkingLot(AuthUser authUser, ParkingLotRequest request) {
        User user = userReader.getActiveUserById(authUser.getId());

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(user)
                .name(request.getName())
                .quantity(request.getQuantity())
                .closedAt(request.getClosedAt())
                .openedAt(request.getOpenedAt())
                .pricePerHour(request.getPricePerHour())
                .description(request.getDescription())
                .address(request.getAddress())
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.OWNER_REGISTERED)
                .build();

        ParkingLotImage defaultImage = ParkingLotImage.builder()
                .imageUrl(defaultParkingLotImageUrl)
                .build();
        parkingLot.addImage(defaultImage);

        Geocode geocode = kakaoGeocodeClient.getGeocode(parkingLot.getAddress());
        Double latitude = geocode.getLatitude();
        Double longitude = geocode.getLongitude();
        parkingLot.updateGeocode(latitude, longitude);

        try {
            return ParkingLotResponse.from(parkingLotWriter.createParkingLot(parkingLot));
        } catch (DataIntegrityViolationException e) {
            throw new ParkingEasyException(ParkingLotErrorCode.DUPLICATED_PARKING_LOT_LOCATION);
        }
    }

    // 주차장 다건 조회 (이름, 주소)
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(ParkingLotSearchRequest request, PageRequest pageRequest) {
        String redisKey = ParkingLotSearchRedisKey.generateRedisKey(request.getName(), request.getAddress(),
                request.getUserLatitude(), request.getUserLongitude(), request.getRadiusInMeters(),
                pageRequest.getPage() - 1, pageRequest.getSize());

        // 캐시 조회
        Object cache = redisTemplate.opsForValue().get(redisKey);
        if (cache != null) {
            log.info("Redis cache hit for key : {}", redisKey);
            // 역직렬화 타입 명시
            try {
                return redisObjectMapper.convertValue(cache, new TypeReference<RestPage<ParkingLotSearchResponse>>() {
                });

            } catch (IllegalArgumentException e) {
                log.error("Redis 역직렬화 오류 : {}", e.getMessage());
            }
        } else {
            log.info("Redis cache miss for key : {}", redisKey);
        }
        
        // DB 조회
        Page<ParkingLotSearchResponse> page = parkingLotReader.searchParkingLotsByConditions(request.getName(), request.getAddress(),
                request.getUserLatitude(), request.getUserLongitude(), request.getRadiusInMeters(),
                pageRequest.getPage(), pageRequest.getSize());

        // 캐시 저장
        redisTemplate.opsForValue().set(redisKey, RestPage.from(page), Duration.ofMinutes(CACHE_TTL));

        return page;
    }

    // 주차장 단건 조회
    public ParkingLotSearchResponse searchParkingLotById(Long parkingLotId) {
        return parkingLotReader.searchParkingLotById(parkingLotId);
    }

    // 본인이 소유한 주차장 조회
    public Page<MyParkingLotSearchResponse> getMyParkingLots(AuthUser authUser, PageRequest pageRequest) {
        Long userId = authUser.getId();
        return parkingLotReader.getMyParkingLots(userId, pageRequest.getPage(), pageRequest.getSize());
    }

    // 주차장 수정 (writer 사용x)
    @Transactional
    public void updateParkingLot(AuthUser authUser, Long parkingLotId, ParkingLotRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);

        Geocode geocode = kakaoGeocodeClient.getGeocode(request.getAddress());
        Double latitude = geocode.getLatitude();
        Double longitude = geocode.getLongitude();
        parkingLot.updateGeocode(latitude, longitude);

        parkingLot.update(
                request.getName(), request.getAddress(),
                latitude, longitude,
                request.getOpenedAt(), request.getClosedAt(),
                request.getPricePerHour(), request.getDescription(), request.getQuantity()
        );
    }

    // 주차장 상태 변경 (writer 사용x)
    @Transactional
    public void updateParkingLotStatus(AuthUser authUser, Long parkingLotId, ParkingLotStatusRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        ParkingLotStatus newStatus = ParkingLotStatus.from(request.getStatus());

        if (newStatus == ParkingLotStatus.CLOSED) {
            throw new ParkingEasyException(ParkingLotErrorCode.INVALID_PARKING_LOT_STATUS_CHANGE);
        }

        parkingLot.updateStatus(newStatus);
    }

    // 주차장 이미지 수정 (writer 사용x)
    @Transactional
    public void updateParkingLotImages(AuthUser authUser, Long parkingLotId, ParkingLotImagesRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls.size() > 5) {
            throw new ParkingEasyException(ParkingLotErrorCode.TOO_MANY_PARKING_LOT_IMAGES);
        }

        List<ParkingLotImage> newImages = request.getImageUrls().stream()
                .map(url -> ParkingLotImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }

    // 주차장 삭제
    public void deleteParkingLot(AuthUser authUser, Long parkingLotId) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        parkingLot.updateStatus(ParkingLotStatus.CLOSED);
        parkingLotWriter.deleteParkingLot(parkingLot);
    }

}
