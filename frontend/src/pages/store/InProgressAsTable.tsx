import { useState, Fragment } from "react";
import {
    Check,
    Info,
    ChevronRight,
    ChevronDown,
    Monitor,
    Coffee,
    Snowflake,
    Refrigerator,
} from "lucide-react";
import type { InProgressAsItem, DispatchStatusName } from "../../api/asRequest";

// 포맷 유틸

const fmtNo = (id: number, requestedAt: string) =>
    `AS-${new Date(requestedAt).getFullYear()}-${String(id).padStart(4, "0")}`;

const fmtDate = (iso: string) => {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
};

// 카테고리 아이콘

const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={18} />,
    ESPRESSO: <Coffee size={18} />,
    ICE_MAKER: <Snowflake size={18} />,
    REFRIGERATOR: <Refrigerator size={18} />,
};

// 상태 → 3단계 진행 인덱스 매핑
// 접수 / 배정(엔지니어 배정 + 이동 중 전체) / 수리중

function getStepIndex(status: DispatchStatusName | null): number {
    if (status == null) return 1; // 접수만 완료 (아직 배정 전)

    if (
        status === "PENDING_ACCEPT" ||
        status === "ACCEPTED" ||
        status === "DISPATCHED" ||
        status === "ARRIVED"
    ) {
        return 2; // 배정 완료 ~ 현장 도착까지 전부 "배정" 단계로 취급 (세부 상황은 배지 텍스트로 전달)
    }

    if (status === "REPAIRING") return 3;
    if (status === "COMPLETED") return 3; // 방어적 처리 (정상적으로는 이 목록에 안 내려옴)

    return 1; // CANCELLED 등
}

// 상태 배지 텍스트 + 색

interface StatusBadgeInfo {
    text: string;
    color: string;
    bg: string;
}

function statusBadge(status: DispatchStatusName | null, etaMinutes: number | null): StatusBadgeInfo {
    switch (status) {
        case "PENDING_ACCEPT":
            return { text: "배정 완료 · 엔지니어 수락 대기 중", color: "#B45309", bg: "#FEF6E7" };
        case "ACCEPTED":
            return { text: "엔지니어 배정 완료", color: "#2563EB", bg: "#EFF6FF" };
        case "DISPATCHED":
            return {
                text: etaMinutes ? `엔지니어 이동 중 · ${etaMinutes}분 후 도착` : "엔지니어 이동 중",
                color: "#B45309",
                bg: "#FEF6E7",
            };
        case "ARRIVED":
            return { text: "현장 도착 완료", color: "#2563EB", bg: "#EFF6FF" };
        case "REPAIRING":
            return { text: "수리 진행 중", color: "#B45309", bg: "#FEF6E7" };
        case "COMPLETED":
            return { text: "수리 완료", color: "#16A34A", bg: "#DCFCE7" };
        default:
            return { text: "엔지니어 배정 대기 중", color: "#475569", bg: "#E2E8F0" };
    }
}

// 단계 타임라인

const STEP_LABELS = ["접수", "배정", "수리중"];
const COL_WIDTH = 60; // 원/라벨 칸 고정 폭 — 두 행이 동일 구조라 자동 정렬됨

function StepTimeline({ stepIndex, requestedAt }: { stepIndex: number; requestedAt: string }) {
    return (
        <div style={{ width: "100%" }}>
            {/* 1행: 원 + 연결선 — 같은 행에 있어 세로 중앙이 자동으로 맞음 */}
            <div className="flex items-center">
                {STEP_LABELS.map((label, i) => {
                    const idx = i + 1;
                    const done = idx < stepIndex;
                    const current = idx === stepIndex;
                    const isLast = i === STEP_LABELS.length - 1;

                    const circleColor = done || current ? "#2563EB" : "#CBD5E1";
                    const circleBg = done ? "#2563EB" : "#fff";

                    return (
                        <Fragment key={label}>
                            <div className="flex items-center justify-center shrink-0" style={{ width: COL_WIDTH }}>
                                <div
                                    className={`rounded-full flex items-center justify-center${current ? " step-pulse" : ""}`}
                                    style={{
                                        width: 30,
                                        height: 30,
                                        border: `${current ? 3 : 2}px solid ${circleColor}`,
                                        background: circleBg,
                                        color: done ? "#fff" : circleColor,
                                        fontSize: 12,
                                        fontWeight: 800,
                                    }}
                                >
                                    {done ? <Check size={15} strokeWidth={3} /> : idx}
                                </div>
                            </div>

                            {!isLast && (
                                <div
                                    style={{
                                        flex: 1,
                                        height: 2,
                                        background: idx < stepIndex ? "#93B4F0" : "#EDF1F5",
                                    }}
                                />
                            )}
                        </Fragment>
                    );
                })}
            </div>

            {/* 2행: 라벨 + 날짜 — 1행과 동일한 폭 구조라 원 바로 아래 정렬됨 */}
            <div className="flex items-start" style={{ marginTop: 6 }}>
                {STEP_LABELS.map((label, i) => {
                    const idx = i + 1;
                    const done = idx < stepIndex;
                    const current = idx === stepIndex;
                    const isLast = i === STEP_LABELS.length - 1;
                    const textColor = done || current ? "#0F172A" : "#94A3B8";

                    return (
                        <Fragment key={label}>
                            <div className="text-center shrink-0" style={{ width: COL_WIDTH }}>
                                <div style={{ fontSize: 11, fontWeight: 700, color: textColor }}>
                                    {label}
                                </div>
                                <div style={{ fontSize: 10, color: "#94A3B8", marginTop: 1 }}>
                                    {idx === 1
                                        ? fmtDate(requestedAt)
                                        : current
                                            ? "진행 중"
                                            : done
                                                ? "완료"
                                                : "예정"}
                                </div>
                            </div>

                            {!isLast && <div style={{ flex: 1 }} />}
                        </Fragment>
                    );
                })}
            </div>
        </div>
    );
}

// 진행 중인 A/S 테이블

interface InProgressAsTableProps {
    rows: InProgressAsItem[];
    loading: boolean;
    error: string;
    onTrack?: (assignmentId: number | null) => void;
    onViewRequest: (equipmentId: number, equipmentName: string) => void;
    onViewAll?: () => void;
    initialLimit?: number;
}

export function InProgressAsTable({
                                      rows,
                                      loading,
                                      error,
                                      onTrack,
                                      onViewRequest,
                                      onViewAll,
                                      initialLimit = 2,
                                  }: InProgressAsTableProps) {
    const [expanded, setExpanded] = useState(false);
    const visible = expanded ? rows : rows.slice(0, initialLimit);

    return (
        <div
            className="rounded-2xl p-5"
            style={{ background: "#fff", border: "1px solid #E5EAF2", boxShadow: "0 8px 24px rgba(15,23,42,0.04)" }}
        >
            {/* 헤더 */}
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-2">
                    <h2 style={{ fontSize: 17, fontWeight: 800, color: "#0F172A" }}>진행 중인 A/S</h2>
                    {rows.length > 0 && (
                        <span
                            className="rounded-full px-2 py-0.5"
                            style={{ fontSize: 12, fontWeight: 800, color: "#2563EB", background: "#EFF6FF" }}
                        >
                            {rows.length}
                        </span>
                    )}
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

            {/* 상태별 표시 */}
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
                <div className="rounded-xl py-9 text-center" style={{ background: "#F8FAFC", border: "1px dashed #CBD5E1" }}>
                    <p style={{ color: "#64748B", fontSize: 14, fontWeight: 600 }}>진행 중인 A/S가 없습니다.</p>
                </div>
            )}

            {/* 목록 */}
            {!loading && !error && visible.length > 0 && (
                <div className="flex flex-col gap-3">
                    {visible.map((r) => {
                        const stepIndex = getStepIndex(r.currentStatus);
                        const badge = statusBadge(r.currentStatus, r.etaMinutes);

                        return (
                            <div
                                key={r.asRequestId}
                                className="rounded-xl p-4 transition-all hover:shadow-sm"
                                style={{ background: "#F8FAFC", border: "1px solid #EEF2F7" }}
                            >
                                {/* 한 행: 기자재 정보 | 타임라인 | [배지 + 버튼 컬럼] */}
                                <div className="flex items-center gap-4">
                                    {/* 기자재 정보 */}
                                    <div className="flex items-center gap-3 shrink-0" style={{ width: 200 }}>
                                        <div
                                            className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                                            style={{ background: "#fff", color: "#475569", border: "1px solid #E2E8F0" }}
                                        >
                                            {catIcons[r.category]}
                                        </div>
                                        <div style={{ minWidth: 0 }}>
                                            <div
                                                style={{
                                                    fontSize: 15,
                                                    color: "#0F172A",
                                                    fontWeight: 800,
                                                    whiteSpace: "nowrap",
                                                    overflow: "hidden",
                                                    textOverflow: "ellipsis",
                                                }}
                                            >
                                                {r.equipmentName}
                                            </div>
                                            <div
                                                style={{
                                                    fontSize: 12,
                                                    color: "#94A3B8",
                                                    fontFamily: "var(--font-mono)",
                                                    whiteSpace: "nowrap",
                                                    overflow: "hidden",
                                                    textOverflow: "ellipsis",
                                                }}
                                            >
                                                {r.modelName}
                                            </div>
                                            <div style={{ fontSize: 11, color: "#94A3B8", fontFamily: "var(--font-mono)" }}>
                                                {fmtNo(r.asRequestId, r.requestedAt)}
                                            </div>
                                        </div>
                                    </div>

                                    <div style={{ width: 1, alignSelf: "stretch", background: "#E2E8F0" }} />

                                    {/* 타임라인 */}
                                    <div style={{ flex: 1, minWidth: 0 }}>
                                        <div style={{ width: "100%", maxWidth: 700, margin: "0 auto", position: "relative", top: 10 }}>
                                            <StepTimeline stepIndex={stepIndex} requestedAt={r.requestedAt} />
                                        </div>
                                    </div>

                                    <div style={{ width: 1, alignSelf: "stretch", background: "#E2E8F0" }} />

                                    {/* 상태 배지 + 버튼 */}
                                    <div className="flex flex-col items-center gap-2 shrink-0" style={{ width: 160 }}>
                                        <span
                                            className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full"
                                            style={{
                                                background: badge.bg,
                                                color: badge.color,
                                                fontSize: 12,
                                                fontWeight: 700,
                                                whiteSpace: "nowrap",
                                            }}
                                        >
                                            <span
                                                style={{
                                                    width: 6,
                                                    height: 6,
                                                    borderRadius: 999,
                                                    background: badge.color,
                                                    flexShrink: 0,
                                                }}
                                            />
                                            <span style={{ lineHeight: 1 }}>{badge.text}</span>
                                        </span>

                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={() => onTrack?.(r.assignmentId)}
                                                className="transition-all hover:opacity-70"
                                                style={{
                                                    fontSize: 12,
                                                    fontWeight: 700,
                                                    color: "#475569",
                                                    background: "transparent",
                                                    border: "none",
                                                    padding: 0,
                                                }}
                                            >
                                                상세 보기
                                            </button>
                                            <span style={{ color: "#CBD5E1", fontSize: 12 }}>|</span>
                                            <button
                                                onClick={() => onViewRequest(r.equipmentId, r.equipmentName)}
                                                className="transition-all hover:opacity-70"
                                                style={{
                                                    fontSize: 12,
                                                    fontWeight: 700,
                                                    color: "#475569",
                                                    background: "transparent",
                                                    border: "none",
                                                    padding: 0,
                                                }}
                                            >
                                                접수 내용
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* 하단 안내 + 더보기 */}
            {!loading && !error && rows.length > 0 && (
                <div className="pt-4 mt-1 flex items-center justify-between" style={{ borderTop: "1px solid #F1F5F9" }}>
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
