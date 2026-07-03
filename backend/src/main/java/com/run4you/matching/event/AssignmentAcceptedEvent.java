package com.run4you.matching.event;

/**
 * 엔지니어가 A/S 요청을 수락해 배정(Assignment)이 확정(ACCEPTED)됐을 때 발행되는 이벤트.
 *
 * <p>수락 처리는 Redisson 분산 락 + 트랜잭션 안에서 수행되므로, 알림/SSE 발행은
 * 반드시 트랜잭션 커밋 이후(AFTER_COMMIT)에 이뤄져야 한다. 그래서 수락 로직은
 * 이 이벤트만 발행하고, 실제 알림 전송은 {@code @TransactionalEventListener}가 담당한다.
 * (내 도메인④ 출동 상태전이와 동일한 발행 패턴)
 */
public record AssignmentAcceptedEvent(
        Long assignmentId,
        Long asRequestId,
        Long engineerId,
        Long requesterId,
        Long storeId,
        Long brandId
) {}