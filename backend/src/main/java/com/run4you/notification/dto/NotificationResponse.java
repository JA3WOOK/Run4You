package com.run4you.notification.dto;

import com.run4you.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long asRequestId,
        String type,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getAsRequestId(), n.getType().name(),
                n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
