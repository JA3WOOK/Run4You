import { useState, useEffect } from "react";
import { X, Monitor, Coffee, Snowflake, Refrigerator, Calendar, ChevronDown } from "lucide-react";
import { StatusBadge } from "../../components/common/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { getRepairHistory } from "../../api/equipment";
import type { RepairHistory } from "../../api/equipment";

const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={22} />,
    ESPRESSO: <Coffee size={22} />,
    ICE_MAKER: <Snowflake size={22} />,
    REFRIGERATOR: <Refrigerator size={22} />,
};

const asStatusLabel: Record<string, string> = {
    RECEIVED: "접수", MATCHING: "매칭중", ASSIGNED: "배정완료",
    IN_PROGRESS: "진행중", COMPLETED: "완료", CANCELLED: "취소",
};

const fmtDate = (s: string | null) => (s ? s.split("T")[0] : "-");
const fmtCost = (n: number | null) => (n != null ? `₩${n.toLocaleString()}` : "-");

interface Props {
    equipmentId: number;
    category: string;
    onClose: () => void;
}

export function RepairHistoryModal({ equipmentId, category, onClose }: Props) {
    const { accessToken } = useAuth();
    const [data, setData] = useState<RepairHistory | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [expanded, setExpanded] = useState<number | null>(null);

    useEffect(() => {
        if (!accessToken) return;
        setLoading(true);
        getRepairHistory(accessToken, equipmentId)
            .then((res) => { setData(res); setError(""); })
            .catch((err) => { console.error(err); setError("수리 이력을 불러오지 못했습니다."); })
            .finally(() => setLoading(false));
    }, [accessToken, equipmentId]);

    // 상세보기 → 진단서/영수증 이동 (자리만, 진단서 화면 만들 때 연결)
    const handleViewReport = (repairReportId: number | null) => {
        if (!repairReportId) return;
        console.log("진단서/영수증으로 이동:", repairReportId);
        // TODO: 진단서 화면(StoreReceipt) 만든 후 연결
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            style={{ background: "rgba(15,23,42,0.4)" }}
            onClick={onClose}
        >
            <div
                className="rounded-2xl w-full max-w-3xl flex flex-col"
                style={{ background: "#fff", boxShadow: "0 20px 50px rgba(0,0,0,0.25)", maxHeight: "85vh" }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* 헤더 */}
                <div className="flex items-start justify-between px-8 pt-7 pb-5">
                    <div>
                        <h2 style={{ fontSize: 20, fontWeight: 700, color: "#0F172A" }}>수리 이력 조회</h2>
                        <p style={{ fontSize: 14, color: "#64748B", marginTop: 4 }}>
                            기자재별 수리 이력 누적 · 생애주기 확인
                        </p>
                    </div>
                    <button onClick={onClose} className="mt-1">
                        <X size={22} style={{ color: "#94A3B8" }} />
                    </button>
                </div>

                {loading && <p className="px-8 pb-8" style={{ color: "#94A3B8" }}>불러오는 중...</p>}
                {error && <p className="px-8 pb-8" style={{ color: "#DC2626" }}>{error}</p>}

                {!loading && !error && data && (
                    <>
                        {/* 상단 기자재 정보 */}
                        <div className="px-8 pb-5" style={{ borderBottom: "1px solid rgba(15,23,42,0.08)" }}>
                            <div className="flex items-start gap-4">
                                <div className="w-14 h-14 rounded-xl flex items-center justify-center shrink-0" style={{ background: "#F1F5F9" }}>
                                    <span style={{ color: "#475569" }}>{catIcons[category]}</span>
                                </div>
                                <div className="flex-1 grid grid-cols-3 gap-x-6 gap-y-3">
                                    <div>
                                        <div style={{ fontSize: 17, fontWeight: 700, color: "#0F172A" }}>{data.name}</div>
                                        <div style={{ fontSize: 13, color: "#64748B", marginBottom: 6 }}>{data.modelName}</div>
                                        <StatusBadge status={data.status} size="sm" />
                                    </div>
                                    <div className="flex flex-col gap-2">
                                        <InfoRow label="시리얼 번호" value={data.serialNo} mono />
                                        <InfoRow label="구매일" value={fmtDate(data.purchasedAt)} />
                                        <InfoRow label="위치" value={data.storeName} />
                                    </div>
                                    <div className="flex flex-col gap-2">
                                        <InfoRow label="최근 수리일" value={fmtDate(data.lastRepairAt)} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* 수리 이력 목록 */}
                        <div className="px-8 py-5 overflow-y-auto flex-1">
                            <h3 style={{ fontSize: 15, fontWeight: 700, color: "#0F172A", marginBottom: 16 }}>
                                수리 이력 <span style={{ color: "#94A3B8", fontWeight: 500 }}>({data.repairHistoryItems.length}건)</span>
                            </h3>

                            {data.repairHistoryItems.length === 0 ? (
                                <p style={{ color: "#94A3B8", fontSize: 14 }}>수리 이력이 없습니다.</p>
                            ) : (
                                <div className="flex flex-col">
                                    {data.repairHistoryItems.map((item, idx) => {
                                        const isLast = idx === data.repairHistoryItems.length - 1;
                                        const isOpen = expanded === idx;
                                        return (
                                            <div key={idx} className="flex gap-4">
                                                {/* 타임라인 (점 + 세로선) */}
                                                <div className="flex flex-col items-center" style={{ width: 24 }}>
                                                    <div
                                                        className="rounded-full shrink-0"
                                                        style={{
                                                            width: 16, height: 16,
                                                            border: `3px solid ${idx === 0 ? "#DC2626" : "#16A34A"}`,
                                                            background: "#fff",
                                                            marginTop: 4,
                                                        }}
                                                    />
                                                    {!isLast && <div style={{ width: 2, flex: 1, background: "#E2E8F0", marginTop: 4 }} />}
                                                </div>

                                                {/* 내용 */}
                                                <div className="flex-1 pb-6">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div className="flex-1">
                                                            <div style={{ fontSize: 13, color: "#64748B", marginBottom: 4 }}>{fmtDate(item.completedAt)}</div>
                                                            {item.errorCode && (
                                                                <div style={{ fontSize: 13, fontWeight: 600, color: "#DC2626", marginBottom: 2 }}>에러코드: {item.errorCode}</div>
                                                            )}
                                                            <div style={{ fontSize: 15, fontWeight: 600, color: "#0F172A" }}>{item.symptom ?? "-"}</div>
                                                        </div>
                                                        <div style={{ textAlign: "right" }}>
                                                            <div style={{ fontSize: 12, color: "#94A3B8" }}>수리 비용</div>
                                                            <div style={{ fontSize: 15, fontWeight: 700, color: "#0F172A" }}>{fmtCost(item.totalCost)}</div>
                                                        </div>
                                                        <span className="px-2.5 py-1 rounded-md shrink-0" style={{ fontSize: 12, fontWeight: 600, background: "#DCFCE7", color: "#16A34A" }}>
                              {asStatusLabel[item.status] ?? item.status}
                            </span>
                                                        <button onClick={() => setExpanded(isOpen ? null : idx)} className="shrink-0 mt-0.5">
                                                            <ChevronDown size={18} style={{ color: "#94A3B8", transform: isOpen ? "rotate(180deg)" : "none", transition: "transform 0.2s" }} />
                                                        </button>
                                                    </div>

                                                    {/* 펼침: 정비 의견 + 상세보기 */}
                                                    {isOpen && (
                                                        <div className="mt-2 px-4 py-3 rounded-lg" style={{ background: "#F8FAFC" }}>
                                                            <div className="flex items-start justify-between gap-3">
                                                                <div className="flex-1">
                                                                    <div style={{ fontSize: 12, color: "#94A3B8", marginBottom: 4 }}>정비 의견</div>
                                                                    <div style={{ fontSize: 14, color: "#334155" }}>{item.diagnosis ?? "정비 의견이 없습니다."}</div>
                                                                </div>
                                                                {item.repairReportId && (
                                                                    <button
                                                                        onClick={() => handleViewReport(item.repairReportId)}
                                                                        className="shrink-0 transition-all hover:opacity-70"
                                                                        style={{ background: "transparent", color: "#2563EB", fontSize: 13, fontWeight: 600, whiteSpace: "nowrap" }}
                                                                    >
                                                                        진단서 / 영수증 보기 →
                                                                    </button>
                                                                )}
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        {/* 하단 요약 */}
                        <div className="flex items-center justify-between px-8 py-5" style={{ borderTop: "1px solid rgba(15,23,42,0.08)" }}>
                            <div className="flex items-center gap-2" style={{ fontSize: 14, color: "#475569" }}>
                                <Calendar size={16} style={{ color: "#94A3B8" }} />
                                총 수리 <strong style={{ color: "#0F172A" }}>{data.totalRepairCount}회</strong>
                                <span style={{ color: "#CBD5E1" }}>|</span>
                                총 수리 비용 <strong style={{ color: "#0F172A" }}>{fmtCost(data.totalRepairCost)}</strong>
                            </div>
                            <button onClick={onClose} className="px-5 py-2.5 rounded-lg" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.12)", color: "#475569", fontSize: 14, fontWeight: 600 }}>
                                닫기
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

function InfoRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
    return (
        <div className="flex justify-between gap-3">
            <span style={{ fontSize: 13, color: "#94A3B8" }}>{label}</span>
            <span style={{ fontSize: 13, color: "#334155", fontWeight: 500, fontFamily: mono ? "var(--font-mono)" : "inherit" }}>{value}</span>
        </div>
    );
}