package com.run4you.dispatch.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.run4you.dispatch.domain.DispatchStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class DispatchStatusTest {

    @Test
    @DisplayName("정상 출동 흐름 전이를 허용한다: ACCEPTED→DISPATCHED→ARRIVED→REPAIRING→COMPLETED")
    void happyPathTransitions() {
        assertThat(ACCEPTED.canTransitionTo(DISPATCHED)).isTrue();
        assertThat(DISPATCHED.canTransitionTo(ARRIVED)).isTrue();
        assertThat(ARRIVED.canTransitionTo(REPAIRING)).isTrue();
        assertThat(REPAIRING.canTransitionTo(COMPLETED)).isTrue();
    }

    @Test
    @DisplayName("단계 건너뛰기 전이를 차단한다")
    void rejectSkippingSteps() {
        assertThat(DISPATCHED.canTransitionTo(REPAIRING)).isFalse(); // 도착 생략
        assertThat(ACCEPTED.canTransitionTo(ARRIVED)).isFalse();     // 출동 생략
        assertThat(ARRIVED.canTransitionTo(COMPLETED)).isFalse();    // 수리 생략
    }

    @Test
    @DisplayName("활성 단계에서 취소는 가능하지만 수리 개시 이후/종료 상태는 취소 불가")
    void cancelRules() {
        assertThat(ACCEPTED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(DISPATCHED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(ARRIVED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(REPAIRING.canTransitionTo(CANCELLED)).isFalse(); // 수리 시작 후 취소 불가
    }

    @Test
    @DisplayName("종료 상태에서는 어떤 전이도 불가")
    void terminalStatesAreFinal() {
        assertThat(COMPLETED.isTerminal()).isTrue();
        assertThat(CANCELLED.isTerminal()).isTrue();
        for (DispatchStatus s : values()) {
            assertThat(COMPLETED.canTransitionTo(s)).isFalse();
            assertThat(CANCELLED.canTransitionTo(s)).isFalse();
        }
    }

    @Test
    @DisplayName("ETA 산출 대상 상태 판별")
    void etaPhaseFlags() {
        assertThat(DISPATCHED.isEnRoute()).isTrue();
        assertThat(ARRIVED.isEnRoute()).isFalse();
        assertThat(ARRIVED.isOnSiteOrLater()).isTrue();
        assertThat(REPAIRING.isOnSiteOrLater()).isTrue();
        assertThat(COMPLETED.isOnSiteOrLater()).isTrue();
        assertThat(DISPATCHED.isOnSiteOrLater()).isFalse();
    }
}
