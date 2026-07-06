import { useState, useEffect } from "react";
import { Search, Monitor, Coffee, Snowflake, Refrigerator } from "lucide-react";
import { StatusBadge } from "../common/StatusBadge";
import { useAuth } from "../../context/AuthContext";
import { getAdminEquipmentList, type AdminEquipment, type AdminEquipmentSearchParams } from "../../api/equipment";

const catIcons: Record<string, React.ReactNode> = {
    KIOSK: <Monitor size={13} />, ESPRESSO: <Coffee size={13} />, ICE_MAKER: <Snowflake size={13} />, REFRIGERATOR: <Refrigerator size={13} />,
};

type StatusFilter = "ALL" | "OPERATIONAL" | "FAULTY" | "REPAIRING";

export function AdminEquipment() {
    const { accessToken } = useAuth();
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
    const [equipments, setEquipments] = useState<AdminEquipment[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!accessToken) return;

        const params: AdminEquipmentSearchParams = {};
        if (statusFilter !== "ALL") params.status = statusFilter;
        if (search.trim()) params.keyword = search.trim();

        setLoading(true);
        const timer = setTimeout(() => {
            getAdminEquipmentList(accessToken, params)
                .then((data) => {
                    setEquipments(data);
                    setError(null);
                })
                .catch((e) => {
                    console.warn("관리자 기자재 목록 조회 실패:", e);
                    setError("기자재 목록을 불러오지 못했습니다.");
                })
                .finally(() => setLoading(false));
        }, 300); // 검색어 입력 debounce

        return () => clearTimeout(timer);
    }, [accessToken, search, statusFilter]);

    return (
        <div className="flex flex-col gap-5">
            <div>
                <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>기자재 관리</h1>
                <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>전체 브랜드 기자재 · {equipments.length}대</p>
            </div>

            <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 px-3 py-2.5 rounded-xl flex-1 max-w-xs" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.1)" }}>
                    <Search size={14} style={{ color: "#94A3B8" }} />
                    <input
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="기자재명, 매장, 모델 검색..."
                        style={{ border: "none", outline: "none", background: "transparent", fontSize: 13, color: "#0F172A", flex: 1 }}
                    />
                </div>
                <div className="flex items-center gap-1 p-1 rounded-xl" style={{ background: "#F1F5F9" }}>
                    {(["ALL", "OPERATIONAL", "FAULTY", "REPAIRING"] as StatusFilter[]).map((s) => {
                        const labels = { ALL: "전체", OPERATIONAL: "정상", FAULTY: "고장", REPAIRING: "수리중" };
                        return (
                            <button
                                key={s}
                                onClick={() => setStatusFilter(s)}
                                className="px-3 py-1.5 rounded-lg transition-all"
                                style={{
                                    background: statusFilter === s ? "#fff" : "transparent",
                                    color: statusFilter === s ? "#0F172A" : "#64748B",
                                    fontSize: 12,
                                    fontWeight: statusFilter === s ? 600 : 400,
                                    boxShadow: statusFilter === s ? "0 1px 3px rgba(0,0,0,0.08)" : "none",
                                }}
                            >
                                {labels[s]}
                            </button>
                        );
                    })}
                </div>
            </div>

            <div className="rounded-xl overflow-hidden" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
                {loading ? (
                    <div style={{ padding: "40px 16px", textAlign: "center", color: "#94A3B8", fontSize: 13 }}>불러오는 중...</div>
                ) : error ? (
                    <div style={{ padding: "40px 16px", textAlign: "center", color: "#DC2626", fontSize: 13 }}>{error}</div>
                ) : equipments.length === 0 ? (
                    <div style={{ padding: "40px 16px", textAlign: "center", color: "#94A3B8", fontSize: 13 }}>조건에 맞는 기자재가 없습니다.</div>
                ) : (
                    <table className="w-full">
                        <thead>
                        <tr style={{ background: "#F8FAFC" }}>
                            {["매장", "기자재명", "모델", "카테고리", "상태", "설치일", "다음 점검"].map((h) => (
                                <th key={h} style={{ fontSize: 11, color: "#64748B", fontWeight: 600, padding: "12px 16px", textAlign: "left", borderBottom: "1px solid rgba(15,23,42,0.06)" }}>{h}</th>
                            ))}
                        </tr>
                        </thead>
                        <tbody>
                        {equipments.map((e, i) => (
                            <tr
                                key={e.id}
                                className="transition-colors hover:bg-slate-50"
                                style={{ borderBottom: i < equipments.length - 1 ? "1px solid rgba(15,23,42,0.04)" : "none" }}
                            >
                                <td style={{ padding: "12px 16px", fontSize: 13, color: "#0F172A", fontWeight: 500 }}>{e.storeName}</td>
                                <td style={{ padding: "12px 16px", fontSize: 13, color: "#0F172A" }}>{e.name}</td>
                                <td style={{ padding: "12px 16px", fontSize: 12, color: "#64748B" }}>{e.modelName}</td>
                                <td style={{ padding: "12px 16px" }}>
                                        <span className="flex items-center gap-1.5" style={{ fontSize: 12, color: "#64748B" }}>
                                            <span style={{ color: "#94A3B8" }}>{catIcons[e.category]}</span>
                                            {e.category}
                                        </span>
                                </td>
                                <td style={{ padding: "12px 16px" }}>
                                    <StatusBadge status={e.status} size="sm" />
                                </td>
                                <td style={{ padding: "12px 16px", fontSize: 12, color: "#64748B" }}>{e.purchasedAt ?? "-"}</td>
                                <td style={{ padding: "12px 16px", fontSize: 12, color: !e.nextInspectionDate ? "#DC2626" : "#16A34A", fontWeight: 500 }}>
                                    {e.nextInspectionDate ?? "-"}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}