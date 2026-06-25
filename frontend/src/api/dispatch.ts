import axios from 'axios';
import { fetchEventSource } from '@microsoft/fetch-event-source';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}

export const SSE_URL = 'http://localhost:8080/api/notifications/subscribe';

// 출동 상태머신: ACCEPTED → DISPATCHED → ARRIVED → REPAIRING → COMPLETED
export type DispatchStatus =
    | 'PENDING_ACCEPT'
    | 'ACCEPTED'
    | 'DISPATCHED'
    | 'ARRIVED'
    | 'REPAIRING'
    | 'COMPLETED'
    | 'CANCELLED';

// PATCH 요청 바디 (DispatchStatusUpdateRequest)
export interface StatusUpdateRequest {
    status: DispatchStatus;
    latitude?: number;
    longitude?: number;
}

// 위치 핑 바디 (EngineerLocationPing)
export interface LocationPing {
    latitude: number;
    longitude: number;
}

// SSE dispatch/location 페이로드 == 상태변경/위치 응답 (DispatchEventPayload)
export interface DispatchEventPayload {
    assignmentId: number;
    asRequestId: number;
    status: DispatchStatus;
    etaMinutes: number | null;
    latitude: number | null;
    longitude: number | null;
    changedAt: string;
}

// 타임라인 1건 (DispatchTrackingResponse.TimelineItem)
export interface TimelineItem {
    status: DispatchStatus;
    etaMinutes: number | null;
    latitude: number | null;
    longitude: number | null;
    changedAt: string;
}

// GET tracking 응답 (DispatchTrackingResponse)
export interface DispatchTracking {
    assignmentId: number;
    asRequestId: number;
    status: DispatchStatus;
    etaMinutes: number | null;
    latitude: number | null;
    longitude: number | null;
    changedAt: string | null;
    timeline: TimelineItem[];
}

// ── REST ────────────────────────────────────────────────────────────────
// ⚠ Domain④(DispatchStatusController)는 다른 도메인과 달리 ApiResponse 래핑이 없다.
//    → 페이로드를 res.data 그대로 사용 (res.data.data 아님)

// 1. 출동 상태 변경 (엔지니어) — PATCH /assignments/{id}/status
export async function changeStatus(
    token: string,
    assignmentId: number,
    body: StatusUpdateRequest,
): Promise<DispatchEventPayload> {
    const res = await api.patch(`/assignments/${assignmentId}/status`, body, {
        headers: authHeader(token),
    });
    return res.data;
}

// 2. 이동 중 위치만 갱신 (엔지니어) — POST /assignments/{id}/location
//    상태 변경 없이 좌표/ETA 만 갱신 → 점주 지도에 location 이벤트로 push
export async function pingLocation(
    token: string,
    assignmentId: number,
    body: LocationPing,
): Promise<DispatchEventPayload> {
    const res = await api.post(`/assignments/${assignmentId}/location`, body, {
        headers: authHeader(token),
    });
    return res.data;
}

// 3. 실시간 추적 초기 상태 (점주) — GET /assignments/{id}/tracking
export async function getTracking(
    token: string,
    assignmentId: number,
): Promise<DispatchTracking> {
    const res = await api.get(`/assignments/${assignmentId}/tracking`, {
        headers: authHeader(token),
    });
    return res.data;
}

// ── SSE ─────────────────────────────────────────────────────────────────

export interface SseHandlers {
    onDispatch?: (p: DispatchEventPayload) => void; // event: dispatch (상태 전이)
    onLocation?: (p: DispatchEventPayload) => void; // event: location (지도/ETA 갱신)
    onConnected?: () => void;
    onError?: (err: unknown) => void;
}

/**
 * 실시간 알림 SSE 구독 — GET /notifications/subscribe
 *
 * EventSource 는 Authorization 헤더를 못 실어서 fetch-event-source 를 쓴다.
 * 서버가 수신 대상(점주/관제진)을 선별해 push 하므로 "본인 접수 건만 수신"이 보장된다.
 * 반환된 함수를 호출하면 구독을 종료한다(컴포넌트 cleanup 에서 호출).
 */
export function subscribeDispatch(token: string, handlers: SseHandlers): () => void {
    const controller = new AbortController();

    fetchEventSource(SSE_URL, {
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal,
        openWhenHidden: true, // 탭이 백그라운드여도 연결 유지
        async onopen(res) {
            if (res.ok) {
                handlers.onConnected?.();
                return;
            }
            throw new Error(`SSE 연결 실패: ${res.status}`);
        },
        onmessage(ev) {
            if (!ev.data) return; // ':ping' 하트비트(주석)는 여기 안 들어옴
            try {
                if (ev.event === 'dispatch') handlers.onDispatch?.(JSON.parse(ev.data));
                else if (ev.event === 'location') handlers.onLocation?.(JSON.parse(ev.data));
                // 'connected' / 'notification' 등은 여기서 무시
            } catch (e) {
                console.error('[SSE] payload 파싱 실패', ev.event, ev.data, e);
            }
        },
        onerror(err) {
            handlers.onError?.(err);
            // 여기서 throw 하지 않으면 라이브러리가 백오프 후 자동 재연결한다.
        },
    });

    return () => controller.abort();
}
