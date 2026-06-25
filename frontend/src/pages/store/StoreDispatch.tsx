import { useState, useEffect, useRef } from "react";
import { Clock, Phone, Star, Navigation, Zap } from "lucide-react";
import { Stepper } from "../../components/common/Stepper";
import { useAuth } from "../../context/AuthContext";
import {
    getTracking,
    subscribeDispatch,
    type DispatchStatus,
    type TimelineItem,
} from "../../api/dispatch";

// 상태 → Stepper current 인덱스 (COMPLETED 는 4로 두어 4단계 모두 done 처리)
const STATUS_STEP: Record<string, number> = {
    PENDING_ACCEPT: 0,
    ACCEPTED: 0,
    DISPATCHED: 0,
    ARRIVED: 1,
    REPAIRING: 2,
    COMPLETED: 4,
    CANCELLED: 0,
};

const STEP_DEFS: { status: DispatchStatus; label: string; sub: string }[] = [
    { status: "DISPATCHED", label: "출동 시작", sub: "엔지니어 이동 중" },
    { status: "ARRIVED", label: "현장 도착", sub: "매장 도착 확인" },
    { status: "REPAIRING", label: "수리 개시", sub: "부품 교체 작업" },
    { status: "COMPLETED", label: "수리 완료", sub: "완료 후 서명 필요" },
];

// 지도 좌표(viewBox 360x400) — 실제 위경도→픽셀 투영 대신 ETA 진행도로 이동시킨다.
const START = { x: 120, y: 300 }; // 엔지니어 출발 위치
const STORE = { x: 240, y: 120 }; // 내 매장(고정)
const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

function fmtTime(iso?: string | null) {
    if (!iso) return undefined;
    const d = new Date(iso);
    return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function bannerOf(status: DispatchStatus, eta: number | null) {
    switch (status) {
        case "DISPATCHED":
            return { text: `엔지니어가 출동 중입니다 · ETA ${eta ?? "-"}분 예상`, live: true };
        case "ARRIVED":
            return { text: "엔지니어가 현장에 도착했습니다", live: true };
        case "REPAIRING":
            return { text: "수리가 진행 중입니다", live: true };
        case "COMPLETED":
            return { text: "수리가 완료되었습니다", live: false };
        default:
            return { text: "엔지니어 배정을 기다리는 중입니다", live: false };
    }
}

interface MapDotProps { x: number; y: number; label: string; color?: string; pulse?: boolean }
function MapDot({ x, y, label, color = "#2563EB", pulse = false }: MapDotProps) {
    return (
        <g>
            {pulse && (
                <circle cx={x} cy={y} r={14} fill={color} opacity={0.15}>
                    <animate attributeName="r" values="10;18;10" dur="2s" repeatCount="indefinite" />
                    <animate attributeName="opacity" values="0.3;0;0.3" dur="2s" repeatCount="indefinite" />
                </circle>
            )}
            <circle cx={x} cy={y} r={6} fill={color} stroke="#fff" strokeWidth={2} />
            <text x={x + 10} y={y + 4} fontSize={10} fill="#374151" fontWeight={600}>{label}</text>
        </g>
    );
}

// assignmentId: 점주의 진행 중 접수에 대응하는 배정 ID. 상위(목록/홈)에서 내려준다.
export function StoreDispatch({
    assignmentId = 1,
    engineerName = "박성민",
}: {
    assignmentId?: number;
    engineerName?: string;
}) {
    const { accessToken } = useAuth();

    const [status, setStatus] = useState<DispatchStatus>("ACCEPTED");
    const [eta, setEta] = useState<number | null>(null);
    const [timeline, setTimeline] = useState<TimelineItem[]>([]);
    const initialEtaRef = useRef<number | null>(null);

    // 1) 초기 상태 로드
    useEffect(() => {
        if (!accessToken) return;
        getTracking(accessToken, assignmentId)
            .then((t) => {
                setStatus(t.status);
                setEta(t.etaMinutes);
                setTimeline(t.timeline ?? []);
                if (t.etaMinutes != null && initialEtaRef.current == null) {
                    initialEtaRef.current = t.etaMinutes;
                }
            })
            .catch((e) => console.error("추적 초기 로드 실패:", e));
    }, [accessToken, assignmentId]);

    // 2) SSE 구독 (dispatch=상태전이 / location=지도·ETA 갱신)
    useEffect(() => {
        if (!accessToken) return;
        const unsubscribe = subscribeDispatch(accessToken, {
            onDispatch: (p) => {
                if (p.assignmentId !== assignmentId) return;
                setStatus(p.status);
                setEta(p.etaMinutes);
                if (p.etaMinutes != null && initialEtaRef.current == null) {
                    initialEtaRef.current = p.etaMinutes;
                }
                setTimeline((prev) => [
                    ...prev,
                    {
                        status: p.status,
                        etaMinutes: p.etaMinutes,
                        latitude: p.latitude,
                        longitude: p.longitude,
                        changedAt: p.changedAt,
                    },
                ]);
            },
            onLocation: (p) => {
                if (p.assignmentId !== assignmentId) return;
                if (p.etaMinutes != null) setEta(p.etaMinutes);
            },
            onError: (e) => console.warn("[SSE] 재연결 시도 중...", e),
        });
        return unsubscribe;
    }, [accessToken, assignmentId]);

    const current = STATUS_STEP[status] ?? 0;
    const banner = bannerOf(status, eta);

    // ETA 진행도로 엔지니어 마커 이동 (도착 이후엔 매장 위치 고정)
    const progress =
        current >= 1
            ? 1
            : initialEtaRef.current && eta != null
                ? Math.min(1, Math.max(0, 1 - eta / initialEtaRef.current))
                : 0;
    const engineerPos = { x: lerp(START.x, STORE.x, progress), y: lerp(START.y, STORE.y, progress) };

    const steps = STEP_DEFS.map((d) => {
        const hit = timeline.find((t) => t.status === d.status);
        return { label: d.label, sub: d.sub, time: fmtTime(hit?.changedAt) };
    });

    return (
        <div className="flex flex-col gap-6">
            <div>
                <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>실시간 출동 현황</h1>
                <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>
                    접수번호: AS-{assignmentId} · 긴급 수리 진행 현황
                </p>
            </div>

            {/* Alert banner */}
            <div
                className="flex items-center gap-3 px-4 py-3 rounded-xl"
                style={{ background: "#EFF6FF", border: "1px solid #93C5FD" }}
            >
                <Zap size={16} style={{ color: "#2563EB" }} />
                <span style={{ fontSize: 13, color: "#1E40AF", fontWeight: 500 }}>
                    {engineerName} 엔지니어 · {banner.text}
                </span>
                {banner.live && (
                    <span
                        className="ml-auto px-2 py-0.5 rounded-full"
                        style={{ background: "#DBEAFE", fontSize: 11, color: "#2563EB", fontWeight: 600 }}
                    >
                        LIVE
                    </span>
                )}
            </div>

            <div className="grid grid-cols-5 gap-6">
                {/* Stepper left */}
                <div
                    className="col-span-2 rounded-xl p-5 flex flex-col gap-5"
                    style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}
                >
                    <div>
                        <h3 style={{ color: "#0F172A" }}>수리 진행 단계</h3>
                        <p style={{ fontSize: 12, color: "#94A3B8", marginTop: 2 }}>실시간 업데이트</p>
                    </div>
                    <Stepper steps={steps} current={current} vertical />

                    {/* Engineer card */}
                    <div
                        className="rounded-xl p-4 flex items-center gap-3 mt-2"
                        style={{ background: "#F8FAFC", border: "1px solid rgba(15,23,42,0.06)" }}
                    >
                        <div
                            className="w-11 h-11 rounded-full flex items-center justify-center shrink-0"
                            style={{ background: "#1E293B", color: "#CBD5E1", fontWeight: 700 }}
                        >
                            {engineerName.charAt(0)}
                        </div>
                        <div className="flex-1">
                            <div style={{ fontSize: 13, fontWeight: 600, color: "#0F172A" }}>
                                {engineerName} 엔지니어
                            </div>
                            <div className="flex items-center gap-2 mt-0.5">
                                <Star size={11} style={{ color: "#F59E0B" }} />
                                <span style={{ fontSize: 11, color: "#64748B" }}>4.9 · 전문 엔지니어</span>
                            </div>
                            <div className="flex items-center gap-1 mt-0.5">
                                <Navigation size={11} style={{ color: "#94A3B8" }} />
                                <span style={{ fontSize: 11, color: "#94A3B8" }}>
                                    {eta != null ? `현재 ${eta}분 거리` : "도착 완료"}
                                </span>
                            </div>
                        </div>
                        <button
                            className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                            style={{ background: "#2563EB", color: "#fff", fontSize: 12, fontWeight: 600 }}
                        >
                            <Phone size={11} />
                            통화
                        </button>
                    </div>
                </div>

                {/* Map right */}
                <div
                    className="col-span-3 rounded-xl overflow-hidden relative"
                    style={{ background: "#EFF6FF", border: "1px solid rgba(15,23,42,0.08)", minHeight: 400, boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}
                >
                    <svg width="100%" height="100%" viewBox="0 0 360 400" style={{ position: "absolute", inset: 0 }}>
                        <rect width={360} height={400} fill="#F0F4F8" />
                        {[60, 120, 180, 240, 300].map((x) => (
                            <line key={x} x1={x} y1={0} x2={x} y2={400} stroke="#E2E8F0" strokeWidth={8} />
                        ))}
                        {[80, 160, 240, 320].map((y) => (
                            <line key={y} x1={0} y1={y} x2={360} y2={y} stroke="#E2E8F0" strokeWidth={8} />
                        ))}
                        {[[20,20,30,50],[70,20,40,50],[130,20,40,50],[190,20,60,50],[270,20,70,50],
                          [20,90,80,60],[120,90,50,60],[190,90,40,60],[250,90,80,60],
                          [20,180,50,50],[90,180,70,50],[180,180,60,50],[260,180,80,50],
                          [20,260,90,50],[130,260,40,50],[190,260,50,50],[260,260,80,50],
                          [20,340,50,40],[90,340,80,40],[190,340,60,40],[270,340,70,40]].map(([x,y,w,h], i) => (
                            <rect key={i} x={x} y={y} width={w} height={h} rx={4} fill="#E2E8F0" opacity={0.7} />
                        ))}
                        <polyline
                            points={`${engineerPos.x},${engineerPos.y} 220,120 240,120`}
                            fill="none" stroke="#2563EB" strokeWidth={3} strokeDasharray="6,3" opacity={0.6}
                        />
                        <MapDot x={STORE.x} y={STORE.y} label="내 매장" color="#DC2626" />
                        <MapDot x={engineerPos.x} y={engineerPos.y} label={engineerName} color="#2563EB" pulse />
                    </svg>

                    {/* ETA overlay */}
                    <div
                        className="absolute top-4 left-4 px-3 py-2 rounded-lg"
                        style={{ background: "rgba(255,255,255,0.95)", backdropFilter: "blur(8px)", boxShadow: "0 2px 8px rgba(0,0,0,0.1)" }}
                    >
                        <div className="flex items-center gap-2">
                            <Clock size={13} style={{ color: "#2563EB" }} />
                            <span style={{ fontSize: 12, fontWeight: 700, color: "#0F172A" }}>
                                {eta != null ? `ETA ${eta}분` : "도착"}
                            </span>
                        </div>
                        <div style={{ fontSize: 10, color: "#94A3B8", marginTop: 2 }}>거리 기반 추정</div>
                    </div>

                    {/* Legend */}
                    <div
                        className="absolute bottom-4 right-4 flex flex-col gap-1.5 px-3 py-2.5 rounded-lg"
                        style={{ background: "rgba(255,255,255,0.95)", backdropFilter: "blur(8px)" }}
                    >
                        <div className="flex items-center gap-2">
                            <div className="w-2.5 h-2.5 rounded-full" style={{ background: "#DC2626" }} />
                            <span style={{ fontSize: 11, color: "#374151" }}>내 매장</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-2.5 h-2.5 rounded-full" style={{ background: "#2563EB" }} />
                            <span style={{ fontSize: 11, color: "#374151" }}>엔지니어 위치</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
