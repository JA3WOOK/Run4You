import { useState, useEffect, useCallback } from "react";
import { MapPin, Navigation, Zap } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { getActiveDispatches, type ActiveDispatch } from "../../api/admin";
import { subscribeDispatch } from "../../api/dispatch";

// DISPATCH_STATUS → 라벨/색 (디자인 원본 stageColors 계열)
const STATUS_META: Record<string, { label: string; color: string }> = {
    ACCEPTED:   { label: "배정 완료", color: "#64748B" },
    DISPATCHED: { label: "출동 시작", color: "#2563EB" },
    ARRIVED:    { label: "현장 도착", color: "#7C3AED" },
    REPAIRING:  { label: "수리 개시", color: "#D97706" },
    COMPLETED:  { label: "수리 완료", color: "#16A34A" },
    CANCELLED:  { label: "취소",      color: "#94A3B8" },
};

const CATEGORY_LABEL: Record<string, string> = {
    KIOSK: "키오스크",
    ESPRESSO: "에스프레소 머신",
    ICE_MAKER: "제빙기",
    REFRIGERATOR: "냉장고",
};

const ACTIVE = new Set(["ACCEPTED", "DISPATCHED", "ARRIVED", "REPAIRING"]);

/**
 * 통합 관제 대시보드 "실시간 출동 현황".
 * - 진입 시 GET /api/admin/dispatches/active 로 초기 목록(브랜드 범위) 로드
 * - 이후 SSE(dispatch/location) 로 실시간 갱신:
 *   · dispatch(상태 전이) → 목록 진입/이탈 반영 위해 전체 재조회(브랜드 범위, 가벼움)
 *   · location(위치/ETA) → 고빈도이므로 해당 행만 in-place 패치
 */
export function AdminDispatchControl() {
    const { accessToken } = useAuth();
    const [dispatches, setDispatches] = useState<ActiveDispatch[]>([]);
    const [loading, setLoading] = useState(true);

    const reload = useCallback(() => {
        if (!accessToken) return;
        getActiveDispatches(accessToken)
            .then(setDispatches)
            .catch((e) => console.warn("활성 출동 로드 실패:", e))
            .finally(() => setLoading(false));
    }, [accessToken]);

    // 초기 스냅샷
    useEffect(() => { reload(); }, [reload]);

    // 실시간 갱신
    useEffect(() => {
        if (!accessToken) return;
        const unsubscribe = subscribeDispatch(accessToken, {
            onDispatch: () => reload(),
            onLocation: (p) => {
                setDispatches((prev) => prev.map((d) =>
                    d.assignmentId === p.assignmentId
                        ? { ...d, etaMinutes: p.etaMinutes, latitude: p.latitude, longitude: p.longitude }
                        : d
                ));
            },
            onError: (e) => console.warn("[SSE/관제] 재연결 시도 중...", e),
        });
        return unsubscribe;
    }, [accessToken, reload]);

    const activeList = dispatches.filter((d) => ACTIVE.has(d.status));

    return (
        <div
            className="rounded-xl overflow-hidden"
            style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}
        >
            {/* 헤더 */}
            <div className="flex items-center justify-between p-4" style={{ borderBottom: "1px solid rgba(15,23,42,0.06)" }}>
                <div className="flex items-center gap-2">
                    <MapPin size={14} style={{ color: "#64748B" }} />
                    <h3 style={{ color: "#0F172A" }}>실시간 출동 현황</h3>
                    <span style={{ fontSize: 12, color: "#64748B" }}>처리 중 {activeList.length}건</span>
                </div>
                <div className="flex items-center gap-1.5">
                    <div className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: "#DC2626" }} />
                    <span style={{ fontSize: 11, color: "#DC2626", fontWeight: 600 }}>LIVE</span>
                </div>
            </div>

            {/* 목록 */}
            <div className="flex flex-col">
                {loading ? (
                    <div style={{ padding: 24, color: "#64748B", fontSize: 13 }}>불러오는 중...</div>
                ) : activeList.length === 0 ? (
                    <div style={{ padding: 24, color: "#94A3B8", fontSize: 13 }}>
                        현재 진행 중인 출동이 없습니다. 엔지니어가 상태를 변경하면 실시간으로 표시됩니다.
                    </div>
                ) : (
                    activeList.map((d) => {
                        const meta = STATUS_META[d.status] ?? { label: d.status, color: "#64748B" };
                        return (
                            <div
                                key={d.assignmentId}
                                className="flex items-center gap-3 px-4 py-3"
                                style={{ borderBottom: "1px solid rgba(15,23,42,0.04)" }}
                            >
                                <div className="w-1.5 h-1.5 rounded-full shrink-0" style={{ background: meta.color }} />
                                <div style={{ width: 84, fontSize: 12, fontWeight: 600, color: "#0F172A" }}>AS-{d.asRequestId}</div>
                                <div className="flex-1 min-w-0">
                                    <div style={{ fontSize: 13, color: "#0F172A" }}>
                                        {d.storeName} · {d.engineerName ?? "미배정"}
                                        {d.priority === "EMERGENCY" && (
                                            <span className="inline-flex items-center gap-0.5 ml-1.5" style={{ fontSize: 10, color: "#DC2626", fontWeight: 700 }}>
                                                <Zap size={9} /> 긴급
                                            </span>
                                        )}
                                    </div>
                                    <div style={{ fontSize: 11, color: "#94A3B8", marginTop: 1 }}>
                                        {CATEGORY_LABEL[d.equipmentCategory ?? ""] ?? d.equipmentName ?? "기자재"}
                                    </div>
                                </div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: meta.color, width: 72, textAlign: "right" }}>
                                    {meta.label}
                                </div>
                                <div className="flex items-center gap-1" style={{ width: 92, justifyContent: "flex-end" }}>
                                    <Navigation size={11} style={{ color: "#94A3B8" }} />
                                    <span style={{ fontSize: 11, color: "#64748B" }}>
                                        {d.status === "REPAIRING"
                                            ? "수리 중"
                                            : d.etaMinutes != null
                                                ? (d.etaMinutes === 0 ? "도착" : `ETA ${d.etaMinutes}분`)
                                                : "-"}
                                    </span>
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}
