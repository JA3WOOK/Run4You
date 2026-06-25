package com.run4you.dispatch.port;

import java.util.List;

/**
 * 통합 관제 스트림 수신 대상 해석 포트.
 * 특정 브랜드의 출동 이벤트를 받아야 하는 관리자 user_id 목록을 반환한다.
 * (해당 브랜드 BRAND_ADMIN + 전체 SUPER_ADMIN)
 */
public interface ControlCenterGateway {

    List<Long> controlCenterUserIds(Long brandId);
}
