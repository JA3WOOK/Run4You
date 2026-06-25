package com.run4you.dispatch.service;

import com.run4you.dispatch.port.LocationGateway.GeoPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 거리 기반 간이 ETA 산출기 (MVP).
 * Haversine 으로 두 좌표 간 직선 거리를 구하고, 평균 주행 속도로 환산한다.
 * 정밀 ETA(실시간 교통 반영)는 2단계 T Map 연동으로 대체.
 */
@Component
public class EtaCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** 평균 주행 속도(km/h) — 도심 기준 기본 30 */
    private final double avgSpeedKmh;

    public EtaCalculator(@Value("${dispatch.eta.avg-speed-kmh:30}") double avgSpeedKmh) {
        this.avgSpeedKmh = avgSpeedKmh > 0 ? avgSpeedKmh : 30.0;
    }

    /** 두 좌표 간 직선 거리(km) */
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 거리(km) → 분 단위 ETA (올림, 최소 1분) */
    public int etaMinutes(double distanceKm) {
        int minutes = (int) Math.ceil(distanceKm / avgSpeedKmh * 60.0);
        return Math.max(minutes, 1);
    }

    /**
     * 엔지니어 현재 좌표 → 매장 좌표 ETA(분).
     * 좌표가 하나라도 없으면 null(산출 불가)을 반환한다.
     */
    public Integer etaMinutes(GeoPoint engineer, GeoPoint store) {
        if (engineer == null || store == null || !engineer.isValid() || !store.isValid()) {
            return null;
        }
        double d = distanceKm(
                engineer.latitude().doubleValue(), engineer.longitude().doubleValue(),
                store.latitude().doubleValue(), store.longitude().doubleValue());
        return etaMinutes(d);
    }

    public Integer etaMinutes(BigDecimal engLat, BigDecimal engLng, GeoPoint store) {
        if (engLat == null || engLng == null) {
            return null;
        }
        return etaMinutes(new GeoPoint(engLat, engLng), store);
    }
}
