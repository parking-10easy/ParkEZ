package com.parkez.parkinglot.rediscache;

public class ParkingLotSearchRedisKey {

    public static String generateRedisKey(String name, String address,
                                          Double userLatitude, Double userLongitude, Integer radiusInMeters,
                                          int page, int size
    ) {
        return String.format("parking-lots-name:%s-addr:%s-lat:%s-lng:%s-radius:%s-page:%d-size:%d",
                name != null ? "val:" + name : "null",
                address != null ? "val:" + address : "null",
                userLatitude != null ? (int) (userLatitude * 1000) : "null", // 위도/경도는 0.001 단위로 라운딩 (약 100m 단위)
                userLongitude != null ? (int) (userLongitude * 1000) : "null", // 위도/경도는 0.001 단위로 라운딩 (약 100m 단위)
                radiusInMeters != null ? radiusInMeters : "null",
                page,
                size
        );
    }
}
