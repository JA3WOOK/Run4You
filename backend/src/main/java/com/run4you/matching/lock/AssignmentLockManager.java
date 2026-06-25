package com.run4you.matching.lock;

import com.run4you.common.exception.LockAcquisitionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 기반 분산 락 매니저
 *
 * 락 키 규칙: lock:as:{asRequestId}
 * - waitTime:  5초 (락 획득 대기)
 * - leaseTime: 10초 (자동 해제 — 장애 시 데드락 방지)
 *
 * 동일 AS 요청에 여러 엔지니어가 동시에 수락을 시도해도
 * 단 1명에게만 배정이 보장된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentLockManager {

    private static final String LOCK_KEY_PREFIX = "lock:as:";
    private static final long   WAIT_TIME_SEC   = 5L;
    private static final long   LEASE_TIME_SEC  = 10L;

    private final RedissonClient redissonClient;

    /**
     * 분산 락을 획득한 후 주어진 작업을 수행한다.
     *
     * @param asRequestId 락 대상 AS 요청 ID
     * @param action      락 범위 내에서 실행할 로직
     * @return 작업 결과
     * @throws LockAcquisitionFailedException 락 획득 실패 시 (이미 배정 처리 중)
     */
    public <T> T executeWithLock(Long asRequestId, Callable<T> action) {
        String lockKey = LOCK_KEY_PREFIX + asRequestId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_SEC, LEASE_TIME_SEC, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[분산 락] 획득 실패 — asRequestId={}, key={}", asRequestId, lockKey);
                throw new LockAcquisitionFailedException(
                        "현재 다른 엔지니어가 수락 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            log.debug("[분산 락] 획득 성공 — asRequestId={}", asRequestId);
            return action.call();

        } catch (LockAcquisitionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[분산 락] 작업 실행 중 오류 — asRequestId={}", asRequestId, e);
            throw new RuntimeException("배정 처리 중 오류가 발생했습니다.", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[분산 락] 해제 — asRequestId={}", asRequestId);
            }
        }
    }

    /** 락 없이 실행 (테스트·관리 목적) */
    public <T> T executeWithoutLock(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
