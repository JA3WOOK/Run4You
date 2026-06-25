package com.run4you.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 실시간 알림(SSE) 내역. 발행된 알림을 영속 저장하고 읽음 여부를 관리한다. (ERD §4-12)
 */
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "as_request_id")
    private Long asRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(Long recipientId, Long asRequestId, NotificationType type,
                         String title, String message) {
        this.recipientId = recipientId;
        this.asRequestId = asRequestId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = false;
    }

    public void markRead() {
        this.read = true;
    }
}
