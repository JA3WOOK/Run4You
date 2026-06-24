import { useState, useEffect } from "react";
import { Monitor, Coffee, Snowflake, Refrigerator, AlertCircle, Clock, CheckCircle, Plus, Search } from "lucide-react";
import { StatusBadge } from "../../components/common/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { getEquipmentList } from "../../api/equipment";
import type { Equipment, EquipmentListResponse } from "../../api/equipment";
import { EquipmentForm } from "./EquipmentForm";
import { RepairHistoryModal } from "./RepairHistoryModal";

const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={18} />,
    ESPRESSO: <Coffee size={18} />,
    ICE_MAKER: <Snowflake size={18} />,
    REFRIGERATOR: <Refrigerator size={18} />,
};

const catLabels: Record<string, string> = {
    ALL: "전체", KIOSK: "키오스크", ESPRESSO: "에스프레소", ICE_MAKER: "제빙기", REFRIGERATOR: "냉장고",
};

type Category = "ALL" | "KIOSK" | "ESPRESSO" | "ICE_MAKER" | "REFRIGERATOR";

export function StoreHome({ onRequestAS }: { onRequestAS: () => void }) {
    const { accessToken } = useAuth();
    const [data, setData] = useState<EquipmentListResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [cat, setCat] = useState<Category>("ALL");
    const [search, setSearch] = useState("");
    const [showForm, setShowForm] = useState(false);
    const [historyTarget, setHistoryTarget] = useState<Equipment | null>(null);
    const [reload, setReload] = useState(0);

    // 백엔드에서 전체 목록 한 번 불러오기 (필터는 프론트에서)
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

    const allEquipments: Equipment[] = data?.equipments ?? [];

    // 프론트에서 카테고리 + 검색 필터링 (즉각 반응)
    const filtered = allEquipments.filter((eq) => {
        const matchCat = cat === "ALL" || eq.category === cat;
        const keyword = search.trim().toLowerCase();
        const matchSearch =
            keyword === "" ||
            eq.name.toLowerCase().includes(keyword) ||
            eq.modelName.toLowerCase().includes(keyword) ||
            eq.serialNo.toLowerCase().includes(keyword);
        return matchCat && matchSearch;
    });

    return (
        <div className="flex flex-col gap-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>기자재 현황</h1>
                    <p style={{ color: "#64748B", fontSize: 14, marginTop: 2 }}>
                        총 {data?.totalCount ?? 0}대 등록
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    {/* 기자재 등록 버튼 */}
                    <button
                        onClick={() => setShowForm(true)}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg transition-all hover:opacity-90"
                        style={{ background: "#2563EB", color: "#fff", fontSize: 14, fontWeight: 600, boxShadow: "0 1px 2px rgba(37,99,235,0.3)" }}
                    >
                        <Plus size={15} />
                        기자재 등록
                    </button>
                    {/* 긴급 A/S 접수 버튼 */}
                    <button
                        onClick={onRequestAS}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg transition-all hover:opacity-90"
                        style={{ background: "#DC2626", color: "#fff", fontSize: 14, fontWeight: 600, boxShadow: "0 1px 2px rgba(220,38,38,0.3)" }}
                    >
                        <Plus size={15} />
                        긴급 A/S 접수
                    </button>
                </div>
            </div>

            {/* Summary strip */}
            <div className="grid grid-cols-4 gap-5">
                {[
                    { label: "전체", value: data?.totalCount ?? 0, icon: <CheckCircle size={20} />, color: "#2563EB" },
                    { label: "정상 가동", value: data?.operationalCount ?? 0, icon: <CheckCircle size={20} />, color: "#16A34A" },
                    { label: "고장", value: data?.faultyCount ?? 0, icon: <AlertCircle size={20} />, color: "#DC2626" },
                    { label: "수리 중", value: data?.repairingCount ?? 0, icon: <Clock size={20} />, color: "#D97706" },
                ].map((s) => (
                    <div key={s.label} className="rounded-xl p-5 flex items-center gap-3.5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
                        <div className="w-12 h-12 rounded-xl flex items-center justify-center shrink-0" style={{ background: `${s.color}1F` }}>
                            <span style={{ color: s.color }}>{s.icon}</span>
                        </div>
                        <div>
                            <div style={{ fontSize: 26, fontWeight: 700, color: "#0F172A", letterSpacing: "-0.02em" }}>{s.value}</div>
                            <div style={{ fontSize: 14, color: "#475569", fontWeight: 600 }}>{s.label}</div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Filters */}
            <div className="flex items-center gap-3">
                <div className="flex items-center gap-1 p-1.5 rounded-xl" style={{ background: "#F1F5F9" }}>
                    {(["ALL", "KIOSK", "ESPRESSO", "ICE_MAKER", "REFRIGERATOR"] as Category[]).map((c) => (
                        <button
                            key={c}
                            onClick={() => setCat(c)}
                            className="flex items-center gap-2 px-4 py-2.5 rounded-lg transition-all"
                            style={{
                                background: cat === c ? "#fff" : "transparent",
                                color: cat === c ? "#0F172A" : "#64748B",
                                fontSize: 15,
                                fontWeight: cat === c ? 700 : 500,
                                boxShadow: cat === c ? "0 1px 3px rgba(0,0,0,0.08)" : "none",
                            }}
                        >
                            {c !== "ALL" && catIcons[c]}
                            {catLabels[c]}
                        </button>
                    ))}
                </div>
                <div className="flex items-center gap-2 px-3 py-3 rounded-lg flex-1" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)" }}>
                    <Search size={16} style={{ color: "#94A3B8" }} />
                    <input
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
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

            {/* Grid — 4열 */}
            {!loading && !error && (
                <div className="grid grid-cols-4 gap-4">
                    {filtered.map((eq) => (
                        <div
                            key={eq.id}
                            className="rounded-xl p-5 flex flex-col gap-4 transition-all hover:shadow-md"
                            style={{
                                background: "#fff",
                                border: `1px solid ${eq.status === "FAULTY" ? "#FCA5A5" : eq.status === "REPAIRING" ? "#FCD34D" : "rgba(15,23,42,0.08)"}`,
                                boxShadow: "0 1px 3px rgba(0,0,0,0.04)",
                                cursor: "pointer",
                                minHeight: 320,
                            }}
                        >
                            <div className="flex items-start justify-between">
                                <div className="flex items-center gap-3">
                                    <div className="w-11 h-11 rounded-lg flex items-center justify-center" style={{ background: "#F1F5F9" }}>
                                        <span style={{ color: "#475569" }}>{catIcons[eq.category]}</span>
                                    </div>
                                    <div>
                                        <div style={{ fontSize: 16, fontWeight: 700, color: "#0F172A" }}>{eq.name}</div>
                                        <div style={{ fontSize: 13, color: "#64748B" }}>{eq.modelName}</div>
                                    </div>
                                </div>
                                <StatusBadge status={eq.status} size="sm" />
                            </div>

                            {eq.errorCode && (
                                <div className="flex items-center gap-2 px-3 py-2 rounded-lg" style={{ background: "#FEF2F2" }}>
                                    <AlertCircle size={14} style={{ color: "#DC2626" }} />
                                    <span style={{ fontSize: 14, color: "#DC2626", fontWeight: 600 }}>에러코드: {eq.errorCode}</span>
                                </div>
                            )}

                            <div className="flex flex-col gap-2">
                                <div className="flex justify-between">
                                    <span style={{ fontSize: 14, color: "#94A3B8" }}>시리얼 번호</span>
                                    <span style={{ fontSize: 14, color: "#334155", fontFamily: "var(--font-mono)", fontWeight: 500 }}>{eq.serialNo}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span style={{ fontSize: 14, color: "#94A3B8" }}>구매일</span>
                                    <span style={{ fontSize: 14, color: "#334155", fontWeight: 500 }}>{eq.purchasedAt ?? "-"}</span>
                                </div>
                                {eq.nextInspectionDate && (
                                    <div className="flex justify-between">
                                        <span style={{ fontSize: 14, color: "#94A3B8" }}>다음 점검</span>
                                        <span style={{ fontSize: 14, color: "#16A34A", fontWeight: 600 }}>{eq.nextInspectionDate}</span>
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
                                        onClick={onRequestAS}
                                        className="flex-1 py-2 rounded-lg text-center transition-all"
                                        style={{ fontSize: 14, fontWeight: 600, background: "#FEF2F2", color: "#DC2626" }}
                                    >
                                        A/S 접수
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 기자재 등록 폼 모달 */}
            {showForm && (
                <EquipmentForm
                    onClose={() => setShowForm(false)}
                    onSuccess={() => setReload((r) => r + 1)}
                />
            )}

            {/* 수리 이력 모달 */}
            {historyTarget && (
                <RepairHistoryModal
                    equipmentId={historyTarget.id}
                    category={historyTarget.category}
                    onClose={() => setHistoryTarget(null)}
                />
            )}
        </div>
    );
}