package com.run4you.dispatch.port;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 좌표 조회 포트. stores(②), engineer_profiles(③) 소유 테이블을 읽기 전용으로 접근한다.
 */
public interface LocationGateway {

    Optional<GeoPoint> storeLocation(Long storeId);

    Optional<GeoPoint> engineerLocation(Long engineerUserId);

    record GeoPoint(BigDecimal latitude, BigDecimal longitude) {
        public boolean isValid() {
            return latitude != null && longitude != null;
        }
    }
}
