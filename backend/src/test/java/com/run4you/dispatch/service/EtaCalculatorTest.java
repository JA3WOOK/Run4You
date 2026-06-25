package com.run4you.dispatch.service;

import com.run4you.dispatch.port.LocationGateway.GeoPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EtaCalculatorTest {

    private final EtaCalculator calc = new EtaCalculator(30); // 30km/h

    @Test
    @DisplayName("동일 좌표 거리는 0")
    void zeroDistance() {
        assertThat(calc.distanceKm(37.5, 127.0, 37.5, 127.0)).isZero();
    }

    @Test
    @DisplayName("Haversine 거리 근사 검증 (서울시청↔강남역 약 8~9km)")
    void haversineApprox() {
        double d = calc.distanceKm(37.5663, 126.9779, 37.4980, 127.0276);
        assertThat(d).isBetween(8.0, 10.0);
    }

    @Test
    @DisplayName("거리→ETA: 15km @30km/h = 30분, 최소 1분 보장")
    void etaFromDistance() {
        assertThat(calc.etaMinutes(15.0)).isEqualTo(30);
        assertThat(calc.etaMinutes(0.0)).isEqualTo(1); // 하한 1
        assertThat(calc.etaMinutes(0.1)).isEqualTo(1); // 올림 후에도 최소 1
    }

    @Test
    @DisplayName("좌표가 없으면 ETA 는 null")
    void nullWhenMissingCoords() {
        GeoPoint store = new GeoPoint(new BigDecimal("37.4980"), new BigDecimal("127.0276"));
        assertThat(calc.etaMinutes((GeoPoint) null, store)).isNull();
        assertThat(calc.etaMinutes(store, null)).isNull();
        assertThat(calc.etaMinutes((BigDecimal) null, null, store)).isNull();
    }

    @Test
    @DisplayName("유효 좌표 쌍이면 1분 이상 ETA 산출")
    void etaFromGeoPoints() {
        GeoPoint engineer = new GeoPoint(new BigDecimal("37.5663"), new BigDecimal("126.9779"));
        GeoPoint store = new GeoPoint(new BigDecimal("37.4980"), new BigDecimal("127.0276"));
        assertThat(calc.etaMinutes(engineer, store)).isGreaterThanOrEqualTo(1);
    }
}
