package com.run4you.dispatch.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 출동(배정) 상태 머신.
 *
 * <p>핵심 흐름: {@code ACCEPTED → DISPATCHED → ARRIVED → REPAIRING → COMPLETED}
 * (어느 활성 단계에서나 CANCELLED 로 전이 가능, COMPLETED/CANCELLED 는 종료 상태)
 *
 * <p>assignments.status 와 dispatch_status_history.status 가 공유하는 값으로,
 * 전이 허용 여부 판정 로직을 이 enum 한 곳에서만 관리한다.
 */
public enum DispatchStatus {

    PENDING_ACCEPT(false),
    ACCEPTED(false),
    DISPATCHED(false),
    ARRIVED(false),
    REPAIRING(false),
    COMPLETED(true),
    CANCELLED(true);

    private final boolean terminal;

    DispatchStatus(boolean terminal) {
        this.terminal = terminal;
    }

    /** 현재 상태 → 전이 허용 대상 집합 */
    private static final Map<DispatchStatus, Set<DispatchStatus>> GRAPH = Map.of(
            PENDING_ACCEPT, EnumSet.of(ACCEPTED, CANCELLED),
            ACCEPTED,       EnumSet.of(DISPATCHED, CANCELLED),
            DISPATCHED,     EnumSet.of(ARRIVED, CANCELLED),
            ARRIVED,        EnumSet.of(REPAIRING, CANCELLED),
            REPAIRING,      EnumSet.of(COMPLETED),
            COMPLETED,      EnumSet.noneOf(DispatchStatus.class),
            CANCELLED,      EnumSet.noneOf(DispatchStatus.class)
    );

    public boolean canTransitionTo(DispatchStatus target) {
        return GRAPH.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return terminal;
    }

    /** 이동(출동) 중 상태 — ETA 산출 대상 */
    public boolean isEnRoute() {
        return this == DISPATCHED;
    }

    /** 현장 도착 이후(도착 포함) — ETA = 0 */
    public boolean isOnSiteOrLater() {
        return this == ARRIVED || this == REPAIRING || this == COMPLETED;
    }
}
