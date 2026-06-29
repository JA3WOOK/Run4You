import { useState } from "react";
import {
    Truck, Wrench, ClipboardCheck, MapPin, CheckCircle2, Info, ChevronRight, ChevronDown,
    Monitor, Coffee, Snowflake, Refrigerator,
} from "lucide-react";
import type { InProgressAsItem, DispatchStatusName } from "../../api/asRequest";

const fmtNo = (id: number, requestedAt: string) =>
    `AS-${new Date(requestedAt).getFullYear()}-${String(id).padStart(4, "0")}`;

const pad = (n: number) => String(n).padStart(2, "0");

// 카테고리 → 아이콘 (기자재 칸)
const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={20} />,
    ESPRESSO: <Coffee size={20} />,
    ICE_MAKER: <Snowflake size={20} />,
    REFRIGERATOR: <Refrigerator size={20} />,
};

// 상태 → 아이콘 + 라벨 + 설명 + 색 (상태 칸)
function statusView(status: DispatchStatusName | null) {
    switch (status) {
        case "DISPATCHED":
            return { icon: <Truck size={16} />, label: "출동 중", desc: "엔지니어가 이동 중입니다.", color: "#D97706", bg: "#FEF3C7" };
        case "ARRIVED":
            return { icon: <MapPin size={16} />, label: "현장 도착", desc: "엔지니어가 도착했습니다.", color: "#2563EB", bg: "#EFF6FF" };
        case "REPAIRING":
            return { icon: <Wrench size={16} />, label: "수리 중", desc: "수리 작업이 진행 중입니다.", color: "#D97706", bg: "#FEF3C7" };
        case "COMPLETED":
            return { icon: <CheckCircle2 size={16} />, label: "수리 완료", desc: "수리가 완료되었습니다.", color: "#16A34A", bg: "#DCFCE7" };
        default:
            return { icon: <ClipboardCheck size={16} />, label: "접수 완료", desc: "엔지니어 배정 대기 중입니다.", color: "#64748B", bg: "#F1F5F9" };
    }
}

// ETA 칸 — 상태별 분기
function etaView(status: DispatchStatusName | null, etaMinutes: number | null) {
    if (status === "DISPATCHED" && etaMinutes != null && etaMinutes > 0) {
        return { title: "예상 도착 (ETA)", value: `${etaMinutes}분 남음` };
    }
    if (status === "ARRIVED") return { title: "예상 시간", value: "도착 완료" };
    if (status === "REPAIRING") {
        const t = new Date(Date.now() + (etaMinutes ?? 0) * 60000);
        return { title: "예상 완료 시간", value: etaMinutes ? `오늘 ${pad(t.getHours())}:${pad(t.getMinutes())}` : "수리 진행 중" };
    }
    return { title: "예상 시간", value: "배정 대기 중" };
}

export function InProgressAsTable({
                                      rows, loading, error, onTrack, onViewRequest, onViewAll, initialLimit = 2,
                                  }: {
    rows: InProgressAsItem[];
    loading: boolean;
    error: string;
    onTrack?: (assignmentId: number | null) => void;
    onViewRequest: (equipmentId: number, equipmentName: string) => void;
    onViewAll?: () => void;
    initialLimit?: number;
}) {
    const [expanded, setExpanded] = useState(false);
    const visible = expanded ? rows : rows.slice(0, initialLimit);

    return (
        <div
            className="rounded-2xl p-5"
            style={{
                background: "#fff",
                border: "1px solid #E5EAF2",
                boxShadow: "0 8px 24px rgba(15,23,42,0.04)",
            }}
        >
            <div className="flex items-center justify-between mb-5">
                <div>
                    <h2 style={{ fontSize: 18, fontWeight: 800, color: "#0F172A" }}>
                        진행 중인 A/S
                    </h2>
                    <p style={{ fontSize: 13, color: "#94A3B8", marginTop: 4 }}>
                        현재 처리 중인 접수 건을 확인하세요.
                    </p>
                </div>

                {onViewAll && (
                    <button
                        onClick={onViewAll}
                        className="flex items-center gap-1 transition-all hover:opacity-70"
                        style={{ fontSize: 13, color: "#2563EB", fontWeight: 700 }}
                    >
                        전체 보기 <ChevronRight size={15} />
                    </button>
                )}
            </div>

            {loading && (
                <p className="py-8 text-center" style={{ color: "#94A3B8", fontSize: 14 }}>
                    불러오는 중...
                </p>
            )}

            {error && (
                <p className="py-8 text-center" style={{ color: "#DC2626", fontSize: 14 }}>
                    {error}
                </p>
            )}

            {!loading && !error && rows.length === 0 && (
                <div
                    className="rounded-xl py-10 text-center"
                    style={{ background: "#F8FAFC", border: "1px dashed #CBD5E1" }}
                >
                    <p style={{ color: "#64748B", fontSize: 14, fontWeight: 600 }}>
                        진행 중인 A/S가 없습니다.
                    </p>
                </div>
            )}

            {!loading && !error && visible.length > 0 && (
                <div className="flex flex-col">
                    {visible.map((r) => {
                        const sv = statusView(r.currentStatus);
                        const ev = etaView(r.currentStatus, r.etaMinutes);
                        const enRoute =
                            r.currentStatus === "DISPATCHED" || r.currentStatus === "ARRIVED";

                        return (
                            <div
                                key={r.asRequestId}
                                className="flex items-center gap-5 py-4"
                                style={{ borderTop: "1px solid #F1F5F9" }}
                            >
                                {/* 기자재 */}
                                <div className="flex items-center gap-3" style={{ width: 550 }}>
                                    <div
                                        className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                                        style={{
                                            background: "#F8FAFC",
                                            color: "#475569",
                                            border: "1px solid #E2E8F0",
                                        }}
                                    >
                                        {catIcons[r.category]}
                                    </div>
                                    <div style={{ minWidth: 0 }}>
                                        <div style={{ fontSize: 15, color: "#0F172A", fontWeight: 800 }}>
                                            {r.equipmentName}
                                        </div>
                                        <div style={{ fontSize: 13, color: "#64748B", marginTop: 2 }}>
                                            {r.modelName}
                                        </div>
                                        <div
                                            style={{
                                                fontSize: 12, color: "#94A3B8", fontWeight: 600,
                                                marginTop: 3, fontFamily: "var(--font-mono)",
                                            }}
                                        >
                                            {fmtNo(r.asRequestId, r.requestedAt)}
                                        </div>
                                    </div>
                                </div>

                                {/* 상태 */}
                                <div className="flex flex-col items-start" style={{ width: 500 }}>
                                    <div
                                        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full"
                                        style={{ background: sv.bg, color: sv.color }}
                                    >
                                        {sv.icon}
                                        <span style={{ fontSize: 13, fontWeight: 800 }}>{sv.label}</span>
                                    </div>
                                    <div style={{ fontSize: 12, color: "#94A3B8", marginTop: 6 }}>
                                        {sv.desc}
                                    </div>
                                </div>

                                {/* ETA — 왼쪽 정렬 */}
                                <div className="flex flex-col items-start" style={{ width: 160 }}>
                                    <div style={{ fontSize: 12, color: "#94A3B8", fontWeight: 600 }}>
                                        {ev.title}
                                    </div>
                                    <div style={{ fontSize: 15, color: "#0F172A", fontWeight: 800, marginTop: 4 }}>
                                        {ev.value}
                                    </div>
                                </div>

                                {/* 버튼  */}
                                <div className="flex items-center gap-1 ml-auto">
                                    <button
                                        onClick={() => onTrack?.(r.assignmentId)}
                                        className="px-3 py-2 rounded-md transition-all hover:bg-slate-100"
                                        style={{
                                            fontSize: 13, fontWeight: 700,
                                            color: enRoute ? "#2563EB" : "#475569",
                                            background: "transparent", border: "none",
                                        }}
                                    >
                                        {enRoute ? "실시간 보기" : "상세 보기"}
                                    </button>
                                    <div style={{ width: 1, height: 14, background: "#E2E8F0" }} />
                                    <button
                                        onClick={() => onViewRequest(r.equipmentId, r.equipmentName)}
                                        className="px-3 py-2 rounded-md transition-all hover:bg-slate-100"
                                        style={{
                                            fontSize: 13, fontWeight: 700,
                                            color: "#475569", background: "transparent", border: "none",
                                        }}
                                    >
                                        접수 내용
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {!loading && !error && rows.length > 0 && (
                <div
                    className="pt-4 mt-1 flex items-center justify-between"
                    style={{ borderTop: "1px solid #F1F5F9" }}
                >
                    <div className="flex items-center gap-2">
                        <Info size={14} style={{ color: "#94A3B8" }} />
                        <span style={{ fontSize: 13, color: "#94A3B8" }}>
                            실시간 위치와 정확한 진행 상황은 "출동 현황"에서 확인하실 수 있습니다.
                        </span>
                    </div>

                    {rows.length > initialLimit && (
                        <button
                            onClick={() => setExpanded((v) => !v)}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all hover:bg-slate-50 shrink-0"
                            style={{ fontSize: 13, color: "#2563EB", fontWeight: 700 }}
                        >
                            {expanded ? "접기" : `더보기 (${rows.length - initialLimit}건 더)`}
                            <ChevronDown
                                size={16}
                                style={{
                                    transform: expanded ? "rotate(180deg)" : "none",
                                    transition: "transform 0.2s",
                                }}
                            />
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}