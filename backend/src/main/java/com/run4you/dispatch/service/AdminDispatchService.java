package com.run4you.dispatch.service;

import com.run4you.dispatch.dto.ActiveDispatchResponse;
import com.run4you.dispatch.port.AdminDispatchQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 통합 관제 대시보드용 활성 출동 조회 서비스.
 * 초기 스냅샷을 REST 로 제공하고, 이후 변경분은 기존 SSE(dispatch/location)로 실시간 갱신된다.
 */
@Service
@RequiredArgsConstructor
public class AdminDispatchService {

    private final AdminDispatchQueryPort adminDispatchQueryPort;

    @Transactional(readOnly = true)
    public List<ActiveDispatchResponse> getActiveDispatches(Long adminUserId) {
        return adminDispatchQueryPort.findActiveForAdmin(adminUserId);
    }
}
