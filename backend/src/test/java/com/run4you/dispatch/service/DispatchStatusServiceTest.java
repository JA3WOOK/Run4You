package com.run4you.dispatch.service;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.dto.DispatchStatusUpdateRequest;
import com.run4you.dispatch.entity.DispatchStatusHistory;
import com.run4you.dispatch.exception.DispatchException;
import com.run4you.dispatch.port.AssignmentGateway;
import com.run4you.dispatch.port.AssignmentGateway.AssignmentView;
import com.run4you.dispatch.port.LocationGateway;
import com.run4you.dispatch.port.LocationGateway.GeoPoint;
import com.run4you.dispatch.repository.DispatchStatusHistoryRepository;
import com.run4you.dispatch.sse.DispatchSsePublisher;
import com.run4you.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchStatusServiceTest {

    @Mock AssignmentGateway assignmentGateway;
    @Mock LocationGateway locationGateway;
    @Mock DispatchStatusHistoryRepository historyRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock DispatchSsePublisher ssePublisher;
    @Mock NotificationService notificationService;

    DispatchStatusService service;

    private static final Long ASSIGN = 42L;
    private static final Long ENGINEER = 7L;

    @BeforeEach
    void setUp() {
        service = new DispatchStatusService(
                assignmentGateway, locationGateway,
                new EtaCalculator(30), historyRepository, eventPublisher,
                ssePublisher, notificationService);
    }

    private AssignmentView view(DispatchStatus current) {
        return new AssignmentView(ASSIGN, 17L, ENGINEER, 3L, 5L, 2L, current);
    }

    private DispatchStatusUpdateRequest req(DispatchStatus target) {
        return new DispatchStatusUpdateRequest(target,
                new BigDecimal("37.5012"), new BigDecimal("127.0396"));
    }

    @Test
    @DisplayName("정상 전이: 이력 적재 + 코어 동기화 + 커밋후 이벤트 발행, ETA 산출")
    void updateStatus_happyPath() {
        when(assignmentGateway.getAssignment(ASSIGN)).thenReturn(view(DispatchStatus.ACCEPTED));
        when(locationGateway.storeLocation(5L)).thenReturn(
                Optional.of(new GeoPoint(new BigDecimal("37.4979"), new BigDecimal("127.0276"))));
        when(historyRepository.save(any(DispatchStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DispatchEventPayload payload = service.updateStatus(ASSIGN, ENGINEER, req(DispatchStatus.DISPATCHED));

        assertThat(payload.status()).isEqualTo("DISPATCHED");
        assertThat(payload.etaMinutes()).isNotNull().isGreaterThanOrEqualTo(1);

        verify(historyRepository).save(any(DispatchStatusHistory.class));
        verify(assignmentGateway).applyDispatchStatus(eq(ASSIGN), eq(DispatchStatus.DISPATCHED), any(LocalDateTime.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("허용되지 않는 전이는 409(INVALID_STATUS_TRANSITION)")
    void updateStatus_invalidTransition() {
        when(assignmentGateway.getAssignment(ASSIGN)).thenReturn(view(DispatchStatus.COMPLETED));

        assertThatThrownBy(() -> service.updateStatus(ASSIGN, ENGINEER, req(DispatchStatus.DISPATCHED)))
                .isInstanceOf(DispatchException.class)
                .extracting(e -> ((DispatchException) e).getCode())
                .isEqualTo("INVALID_STATUS_TRANSITION");

        verify(historyRepository, never()).save(any());
        verify(assignmentGateway, never()).applyDispatchStatus(any(), any(), any());
    }

    @Test
    @DisplayName("MVP: 게이트 없이 ARRIVED→REPAIRING 정상 통과 (도착 이후 ETA=0)")
    void updateStatus_repairing_succeeds() {
        when(assignmentGateway.getAssignment(ASSIGN)).thenReturn(view(DispatchStatus.ARRIVED));
        when(historyRepository.save(any(DispatchStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        DispatchEventPayload payload = service.updateStatus(ASSIGN, ENGINEER, req(DispatchStatus.REPAIRING));

        assertThat(payload.status()).isEqualTo("REPAIRING");
        assertThat(payload.etaMinutes()).isZero();
        verify(assignmentGateway).applyDispatchStatus(eq(ASSIGN), eq(DispatchStatus.REPAIRING), any(LocalDateTime.class));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("본인 배정이 아니면 403(DISPATCH_FORBIDDEN)")
    void updateStatus_forbidden() {
        when(assignmentGateway.getAssignment(ASSIGN)).thenReturn(view(DispatchStatus.ACCEPTED));

        assertThatThrownBy(() -> service.updateStatus(ASSIGN, 999L, req(DispatchStatus.DISPATCHED)))
                .isInstanceOf(DispatchException.class)
                .extracting(e -> ((DispatchException) e).getCode())
                .isEqualTo("DISPATCH_FORBIDDEN");
    }

    @Test
    @DisplayName("배정이 없으면 404(ASSIGNMENT_NOT_FOUND)")
    void updateStatus_notFound() {
        when(assignmentGateway.getAssignment(ASSIGN)).thenReturn(null);

        assertThatThrownBy(() -> service.updateStatus(ASSIGN, ENGINEER, req(DispatchStatus.DISPATCHED)))
                .isInstanceOf(DispatchException.class)
                .extracting(e -> ((DispatchException) e).getCode())
                .isEqualTo("ASSIGNMENT_NOT_FOUND");
    }
}
