package com.run4you.notification.controller;

import com.run4you.dispatch.support.AuthFacade;
import com.run4you.notification.dto.NotificationResponse;
import com.run4you.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 실시간 알림 API.
 * <ul>
 *   <li>GET /api/notifications/subscribe — SSE 구독 (text/event-stream)</li>
 *   <li>GET /api/notifications/me — 알림 목록 + 미읽음 수</li>
 *   <li>PATCH /api/notifications/{id}/read, /read-all — 읽음 처리</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthFacade authFacade;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.subscribe(authFacade.currentUserId());
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> myNotifications(
            @RequestParam(defaultValue = "30") int limit) {
        Long userId = authFacade.currentUserId();
        List<NotificationResponse> items = notificationService.listMine(userId, Math.min(limit, 100));
        return ResponseEntity.ok(Map.of(
                "items", items,
                "unreadCount", notificationService.unreadCount(userId)
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(authFacade.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(authFacade.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
