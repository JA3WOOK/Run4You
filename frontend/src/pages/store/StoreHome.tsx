import { useState, useEffect, useRef } from "react";
import {
    Monitor,
    Coffee,
    Snowflake,
    Refrigerator,
    AlertCircle,
    Plus,
    Search,
    FileWarning,
    ClipboardList,
    ReceiptText,
    RotateCcw,
    ChevronDown,
} from "lucide-react";
import { StatusBadge } from "../../components/common/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { getEquipmentList } from "../../api/equipment";
import type { Equipment, EquipmentListResponse } from "../../api/equipment";
import { getInProgressAsList } from "../../api/asRequest";
import type { InProgressAsItem } from "../../api/asRequest";
import { EquipmentForm } from "./EquipmentForm";
import { RepairHistoryModal } from "./RepairHistoryModal";
import { AsRequestDetailModal } from "./AsRequestDetailModal";
import { InProgressAsTable } from "./InProgressAsTable";
import { getReceipts } from "../../api/receipt";
import { ReviewModal } from "./ReviewModal";

// 카테고리 아이콘 / 라벨
const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={18} />,
    ESPRESSO: <Coffee size={18} />,
    ICE_MAKER: <Snowflake size={18} />,
    REFRIGERATOR: <Refrigerator size={18} />,
};

const catLabels: Record<string, string> = {
    ALL: "전체",
    KIOSK: "키오스크",
    ESPRESSO: "에스프레소",
    ICE_MAKER: "제빙기",
    REFRIGERATOR: "냉장고",
};

type Category = "ALL" | "KIOSK" | "ESPRESSO" | "ICE_MAKER" | "REFRIGERATOR";
type StatusFilter = "ALL" | "OPERATIONAL" | "FAULTY" | "REPAIRING";

const VISIBLE_LIMIT = 4;

interface StoreHomeProps {
    onRequestAS: () => void;
    onGoReceipts: () => void;
    onTrack?: (assignmentId: number | null, engineer?: { name: string | null; phone: string | null }) => void;
    onViewAll?: () => void;
    /** 앱 레벨 SSE 알림 수신 시마다 증가 — 목록/카운트 새로고침 없이 실시간 반영 */
    refreshSignal?: number;
}

export function StoreHome({ onRequestAS, onGoReceipts, onTrack, onViewAll, refreshSignal }: StoreHomeProps) {
    const { accessToken, user } = useAuth();
    const [data, setData] = useState<EquipmentListResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // 진행 중 A/S
    const [inProgress, setInProgress] = useState<InProgressAsItem[]>([]);
    const [ipLoading, setIpLoading] = useState(true);
    const [ipError, setIpError] = useState("");

    const [cat, setCat] = useState<Category>("ALL");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
    const [search, setSearch] = useState("");
    const [showForm, setShowForm] = useState(false);
    const [historyTarget, setHistoryTarget] = useState<Equipment | null>(null);
    const [asDetail, setAsDetail] = useState<{ equipmentId: number; equipmentName: string } | null>(null);
    const [reviewTarget, setReviewTarget] = useState<number | null>(null);
    const [reload, setReload] = useState(0);
    const [doneCount, setDoneCount] = useState<number | null>(null);
    const [showAll, setShowAll] = useState(false);

    // 앱 레벨 SSE 알림이 올 때마다 목록/진행중/완료수 재조회 (새로고침 불필요)
    useEffect(() => {
        if (refreshSignal === undefined) return;
        setReload((v) => v + 1);
    }, [refreshSignal]);

    const gridRef = useRef<HTMLDivElement>(null);
    const inProgressRef = useRef<HTMLDivElement>(null);

    // 기자재 목록
    useEffect(() => {
        if (!accessToken) return;
        setLoading(true);
        getEquipmentList(accessToken)
            .then((res) => {
                setData(res);
                setError("");
            })
            .catch((err) => {
                console.error(err);
                setError("기자재 목록을 불러오지 못했습니다.");
            })
            .finally(() => setLoading(false));
    }, [accessToken, reload]);

    // 진행 중 A/S
    useEffect(() => {
        if (!accessToken) return;
        setIpLoading(true);
        getInProgressAsList(accessToken)
            .then((res) => {
                setInProgress(res.requests);
                setIpError("");
            })
            .catch((err) => {
                console.error(err);
                setIpError("진행 중인 A/S를 불러오지 못했습니다.");
            })
            .finally(() => setIpLoading(false));
    }, [accessToken, reload]);

    // 완료 건수 (영수증 목록 개수 재활용)
    useEffect(() => {
        if (!accessToken) return;
        getReceipts(accessToken, {})
            .then((res) => setDoneCount(res.receipts.length))
            .catch(() => setDoneCount(null));
    }, [accessToken, reload]);

    const allEquipments: Equipment[] = data?.equipments ?? [];

    const filtered = allEquipments.filter((eq) => {
        const matchCat = cat === "ALL" || eq.category === cat;
        const matchStatus =
            statusFilter === "ALL" ||
            (statusFilter === "FAULTY" ? (eq.status === "FAULTY" || eq.status === "REPAIRING") : eq.status === statusFilter);
        const keyword = search.trim().toLowerCase();
        const matchSearch =
            keyword === "" ||
            eq.name.toLowerCase().includes(keyword) ||
            eq.modelName.toLowerCase().includes(keyword) ||
            eq.serialNo.toLowerCase().includes(keyword);
        return matchCat && matchStatus && matchSearch;
    });

    // 더보기 — 기본 5개, 펼치면 전체
    const visibleEquipments = showAll ? filtered : filtered.slice(0, VISIBLE_LIMIT);

    // 상단 카드 → 하단 그리드 필터 + 스크롤
    const applyFilter = (s: StatusFilter) => {
        setStatusFilter(s);
        setCat("ALL");
        setSearch("");
        setShowAll(false);
        setTimeout(() => gridRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 50);
    };

    const scrollToInProgress = () => {
        inProgressRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    const resetFilters = () => {
        setStatusFilter("ALL");
        setCat("ALL");
        setSearch("");
        setShowAll(false);
    };

    // 상단 3카드
    const summaryCards = [
        {
            key: "FAULTY" as const,
            title: "고장 장비",
            sub: "고장 또는 수리 중인 기자재",
            value: (data?.faultyCount ?? 0) + (data?.repairingCount ?? 0),
            icon: <FileWarning size={20} />,
            color: "#DC2626",
            cta: "고장 장비 보기",
            onClick: () => applyFilter("FAULTY"),
        },
        {
            key: "REPAIRING" as const,
            title: "진행 중 A/S",
            sub: "현재 처리 중인 A/S 현황",
            value: inProgress.length,
            icon: <ClipboardList size={20} />,
            color: "#3B82F6",
            cta: "현황 보기",
            onClick: scrollToInProgress,
        },
        {
            key: "DONE" as const,
            title: "완료 내역",
            sub: "수리 완료된 진단서·영수증",
            value: doneCount,
            icon: <ReceiptText size={20} />,
            color: "#22C55E",
            cta: "영수증 보기",
            onClick: onGoReceipts,
        },
    ];

    return (
        <div className="flex flex-col gap-5" style={{ maxWidth: 1300, margin: "0 auto", width: "100%" }}>
            {/* 인사 + 액션 */}
            <div className="flex items-start justify-between">
                <div>
                    <p style={{ color: "#94A3B8", fontSize: 13, fontWeight: 600, marginBottom: 4 }}>
                        {data?.storeName}
                    </p>
                    <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>
                        안녕하세요, {user?.name ?? "점주"} 점주님!
                    </h1>
                    <p style={{ color: "#64748B", fontSize: 14, marginTop: 4 }}>
                        매장 A/S 현황을 한눈에 확인하세요.
                    </p>
                </div>

                <div className="flex items-center gap-2">
                    <button
                        onClick={() => setShowForm(true)}
                        className="flex items-center gap-2 px-5 py-2.5 rounded-lg transition-all hover:opacity-90"
                        style={{
                            background: "#2563EB",
                            color: "#fff",
                            fontSize: 15,
                            fontWeight: 600,
                            boxShadow: "0 1px 2px rgba(37,99,235,0.3)",
                        }}
                    >
                        <Plus size={15} /> 기자재 등록
                    </button>
                    <button
                        onClick={onRequestAS}
                        className="flex items-center gap-2 px-5 py-2.5 rounded-lg transition-all hover:opacity-90"
                        style={{
                            background: "#DC2626",
                            color: "#fff",
                            fontSize: 15,
                            fontWeight: 600,
                            boxShadow: "0 1px 2px rgba(220,38,38,0.3)",
                        }}
                    >
                        <Plus size={15} /> 긴급 A/S 접수
                    </button>
                </div>
            </div>

            {/* 상단 3카드 */}
            <div className="grid grid-cols-3 gap-4">
                {summaryCards.map((c) => (
                    <button
                        key={c.key}
                        onClick={c.onClick}
                        className="rounded-2xl px-4 py-3 text-left transition-all hover:-translate-y-0.5 hover:shadow-md"
                        style={{
                            background: "#fff",
                            border: "1px solid #E2E8F0",
                            boxShadow: "0 1px 2px rgba(15,23,42,0.04)",
                        }}
                    >
                        {/* 제목 + 아이콘 */}
                        <div className="flex items-center justify-between w-full">
                            <div className="flex items-center gap-3">
                                <div
                                    className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
                                    style={{ background: `${c.color}15`, color: c.color }}
                                >
                                    {c.icon}
                                </div>

                                <div>
                                    <div style={{ fontSize: 14, fontWeight: 700, color: "#0F172A" }}>
                                        {c.title}
                                    </div>
                                    <p style={{ fontSize: 12, color: "#94A3B8", marginTop: 2 }}>
                                        {c.sub}
                                    </p>
                                </div>
                            </div>

                            <div className="flex items-baseline gap-1.5">
                                <span
                                    style={{
                                        fontSize: 28,
                                        fontWeight: 800,
                                        color: c.color,
                                        letterSpacing: "-0.03em",
                                    }}
                                >
                                    {c.value ?? "-"}
                                </span>
                                <span style={{ fontSize: 13, color: "#64748B", fontWeight: 600 }}>
                                    건
                                </span>
                            </div>
                        </div>
                    </button>
                ))}
            </div>

            {/* 진행 중인 A/S */}
            <div ref={inProgressRef}>
                <InProgressAsTable
                    rows={inProgress}
                    loading={ipLoading}
                    error={ipError}
                    onTrack={(assignmentId) => {
                        const row = inProgress.find((r) => r.assignmentId === assignmentId);
                        onTrack?.(assignmentId, { name: row?.engineerName ?? null, phone: row?.engineerPhone ?? null });
                    }}
                    onViewAll={onViewAll}
                    onViewRequest={(equipmentId, equipmentName) => setAsDetail({ equipmentId, equipmentName })}
                    onReview={(asRequestId) => setReviewTarget(asRequestId)}
                />
            </div>

            {/* 등록 기자재 */}
            <div ref={gridRef} className="flex flex-col">
                <div className="flex items-center justify-between mb-2">
                    <div>
                        <h2 style={{ fontSize: 18, fontWeight: 700, color: "#0F172A" }}>등록 기자재</h2>
                        <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>
                            총 {data?.totalCount ?? 0}대 등록
                        </p>
                    </div>
                    <button
                        onClick={resetFilters}
                        className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-all hover:bg-slate-100"
                        style={{ fontSize: 13, color: "#475569", fontWeight: 600, border: "1px solid rgba(15,23,42,0.12)" }}
                    >
                        <RotateCcw size={14} /> 필터 초기화
                    </button>
                </div>

                {/* 카테고리 + 검색 */}
                <div className="flex items-center gap-3 mb-4">
                    <div className="flex items-center gap-1 p-1 rounded-xl" style={{ background: "#F1F5F9" }}>
                        {(["ALL", "KIOSK", "ESPRESSO", "ICE_MAKER", "REFRIGERATOR"] as Category[]).map((c) => (
                            <button
                                key={c}
                                onClick={() => {
                                    setCat(c);
                                    setShowAll(false);
                                }}
                                className="flex items-center gap-2 px-3 py-1.5 rounded-lg transition-all"
                                style={{
                                    background: cat === c ? "#fff" : "transparent",
                                    color: cat === c ? "#0F172A" : "#64748B",
                                    fontSize: 14,
                                    fontWeight: cat === c ? 700 : 500,
                                    boxShadow: cat === c ? "0 1px 3px rgba(0,0,0,0.08)" : "none",
                                }}
                            >
                                {c !== "ALL" && catIcons[c]}
                                {catLabels[c]}
                            </button>
                        ))}
                    </div>

                    <div
                        className="flex items-center gap-2 px-3 py-2 rounded-lg flex-1"
                        style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)" }}
                    >
                        <Search size={16} style={{ color: "#94A3B8" }} />
                        <input
                            value={search}
                            onChange={(e) => {
                                setSearch(e.target.value);
                                setShowAll(false);
                            }}
                            placeholder="기기명, 모델, 시리얼 검색..."
                            style={{ border: "none", outline: "none", background: "transparent", fontSize: 14, color: "#0F172A", flex: 1 }}
                        />
                    </div>
                </div>

                {/* 상태 표시 */}
                {loading && <p style={{ color: "#94A3B8", fontSize: 14 }}>불러오는 중...</p>}
                {error && <p style={{ color: "#DC2626", fontSize: 14 }}>{error}</p>}
                {!loading && !error && filtered.length === 0 && (
                    <p style={{ color: "#94A3B8", fontSize: 14 }}>조건에 맞는 기자재가 없습니다.</p>
                )}

                {/* 그리드 */}
                {!loading && !error && (
                    <div className="grid grid-cols-4 gap-4">
                        {visibleEquipments.map((eq) => (
                            <div
                                key={eq.id}
                                className="rounded-xl p-5 flex flex-col gap-4 transition-all hover:shadow-md"
                                style={{
                                    background: "#fff",
                                    border: `1px solid ${
                                        eq.status === "FAULTY"
                                            ? "#FCA5A5"
                                            : eq.status === "REPAIRING"
                                                ? "#FCD34D"
                                                : "rgba(15,23,42,0.08)"
                                    }`,
                                    boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
                                    cursor: "pointer",
                                    minHeight: 320,
                                }}
                            >
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-3 min-w-0">
                                        <div
                                            className="w-11 h-11 rounded-lg flex items-center justify-center shrink-0"
                                            style={{ background: "#F1F5F9" }}
                                        >
                                            <span style={{ color: "#475569" }}>{catIcons[eq.category]}</span>
                                        </div>
                                        <div className="min-w-0">
                                            <div style={{ fontSize: 16, fontWeight: 700, color: "#0F172A" }} className="truncate">
                                                {eq.name}
                                            </div>
                                            <div style={{ fontSize: 13, color: "#64748B" }}>{eq.modelName}</div>
                                        </div>
                                    </div>
                                    <div className="shrink-0">
                                        <StatusBadge status={eq.status} size="sm" />
                                    </div>
                                </div>

                                {eq.errorCode && eq.status !== "OPERATIONAL" && (
                                    <div className="flex items-center gap-2 px-3 py-2 rounded-lg" style={{ background: "#FEF2F2" }}>
                                        <AlertCircle size={14} style={{ color: "#DC2626" }} />
                                        <span style={{ fontSize: 14, color: "#DC2626", fontWeight: 600 }}>
                                            에러코드: {eq.errorCode}
                                        </span>
                                    </div>
                                )}

                                <div className="flex flex-col gap-2">
                                    <div className="flex justify-between">
                                        <span style={{ fontSize: 14, color: "#94A3B8" }}>시리얼 번호</span>
                                        <span style={{ fontSize: 14, color: "#334155", fontFamily: "var(--font-mono)", fontWeight: 500 }}>
                                            {eq.serialNo}
                                        </span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span style={{ fontSize: 14, color: "#94A3B8" }}>구매일</span>
                                        <span style={{ fontSize: 14, color: "#334155", fontWeight: 500 }}>
                                            {eq.purchasedAt ?? "-"}
                                        </span>
                                    </div>
                                    {eq.nextInspectionDate && (
                                        <div className="flex justify-between">
                                            <span style={{ fontSize: 14, color: "#94A3B8" }}>다음 점검</span>
                                            <span style={{ fontSize: 14, color: "#16A34A", fontWeight: 600 }}>
                                                {eq.nextInspectionDate}
                                            </span>
                                        </div>
                                    )}
                                </div>

                                <div className="flex gap-2 mt-auto pt-3" style={{ borderTop: "1px solid rgba(15,23,42,0.06)" }}>
                                    <button
                                        onClick={() => setHistoryTarget(eq)}
                                        className="flex-1 py-2 rounded-lg text-center transition-all hover:bg-slate-100"
                                        style={{ fontSize: 13, color: "#475569", fontWeight: 600 }}
                                    >
                                        이력 보기
                                    </button>
                                    {eq.status !== "OPERATIONAL" && (
                                        <button
                                            onClick={() => setAsDetail({ equipmentId: eq.id, equipmentName: eq.name })}
                                            className="flex-1 py-2 rounded-lg text-center transition-all"
                                            style={{ fontSize: 14, fontWeight: 600, background: "#FEF2F2", color: "#DC2626" }}
                                        >
                                            접수 내용
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* 더보기 / 접기 */}
                {!loading && !error && filtered.length > VISIBLE_LIMIT && (
                    <div className="flex justify-center pt-2">
                        <button
                            onClick={() => setShowAll((v) => !v)}
                            className="flex items-center gap-1.5 px-5 py-2.5 rounded-lg transition-all hover:bg-slate-100"
                            style={{ fontSize: 14, color: "#2563EB", fontWeight: 600, border: "1px solid rgba(37,99,235,0.2)" }}
                        >
                            {showAll ? "접기" : `더보기 (${filtered.length - VISIBLE_LIMIT}대 더)`}
                            <ChevronDown
                                size={16}
                                style={{ transform: showAll ? "rotate(180deg)" : "none", transition: "transform 0.2s" }}
                            />
                        </button>
                    </div>
                )}
            </div>

            {/* 모달 */}
            {showForm && <EquipmentForm onClose={() => setShowForm(false)} onSuccess={() => setReload((r) => r + 1)} />}
            {historyTarget && (
                <RepairHistoryModal
                    equipmentId={historyTarget.id}
                    category={historyTarget.category}
                    onClose={() => setHistoryTarget(null)}
                />
            )}
            {asDetail && (
                <AsRequestDetailModal
                    equipmentId={asDetail.equipmentId}
                    equipmentName={asDetail.equipmentName}
                    onClose={() => setAsDetail(null)}
                    onCancelled={() => setReload((r) => r + 1)}
                />
            )}
            {reviewTarget !== null && (
                <ReviewModal
                    asRequestId={reviewTarget}
                    onClose={() => setReviewTarget(null)}
                    onSuccess={() => {
                        setReviewTarget(null);
                        setReload((r) => r + 1);
                    }}
                />
            )}
        </div>
    );
}