import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}

// 백엔드 NotificationType (NotificationService.notifyDispatchEvent 매핑)
export type NotificationType =
    | 'ASSIGNED' | 'DISPATCHED' | 'ARRIVED' | 'REPAIRING' | 'COMPLETED' | string;

// NotificationResponse — SSE 'notification' 페이로드 == /me items 1건
export interface NotificationItem {
    id: number;
    asRequestId: number | null;
    type: NotificationType;
    title: string;
    message: string;
    read: boolean;
    createdAt: string;
}

// GET /me 응답
export interface MyNotifications {
    items: NotificationItem[];
    unreadCount: number;
}

// ⚠ 알림 API는 Domain④(DispatchStatusController)와 동일하게 ApiResponse 래핑이 없다.
//    NotificationController가 ResponseEntity.ok(Map.of(...))를 직접 반환 → res.data 그대로 사용.

// 알림 목록 + 미읽음 수 — GET /notifications/me
export async function getMyNotifications(token: string, limit = 30): Promise<MyNotifications> {
    const res = await api.get(`/notifications/me?limit=${limit}`, { headers: authHeader(token) });
    return res.data;
}

// 전체 읽음 — PATCH /notifications/read-all
export async function markAllNotificationsRead(token: string): Promise<void> {
    await api.patch('/notifications/read-all', null, { headers: authHeader(token) });
}

// 단건 읽음 — PATCH /notifications/{id}/read
export async function markNotificationRead(token: string, id: number): Promise<void> {
    await api.patch(`/notifications/${id}/read`, null, { headers: authHeader(token) });
}
