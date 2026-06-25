package com.run4you.notification.entity;

/**
 * 실시간 알림 유형. (ERD §5 NOTIFICATION_TYPE)
 */
public enum NotificationType {
    ASSIGNED,
    DISPATCHED,
    ARRIVED,
    REPAIRING,
    COMPLETED,
    SETTLEMENT
}
