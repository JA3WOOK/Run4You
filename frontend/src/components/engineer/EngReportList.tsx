import { useEffect, useState } from "react";
import { Wrench, ChevronRight } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { fetchPendingReports, type PendingReport } from "../../api/matching";

export function EngReportList({ onSelect }: { onSelect: (item: PendingReport) => void }) {
    const { accessToken } = useAuth();
    const [items, setItems] = useState<PendingReport[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchPendingReports(accessToken)
            .then(setItems)
            .catch((e) => console.warn(e))
            .finally(() => setLoading(false));
    }, [accessToken]);

    if (loading) return <div style={{ color: "#64748B", fontSize: 14 }}>불러오는 중...</div>;

    if (items.length === 0) {
        return (
            <div className="rounded-xl p-8 text-center" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", color: "#94A3B8", fontSize: 14 }}>
                작성할 정비 리포트가 없습니다.
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-3">
            {items.map((it) => (
                <div
                    key={it.assignmentId}
                    onClick={() => onSelect(it)}
                    className="rounded-xl p-4 flex items-center gap-4 cursor-pointer hover:shadow-md transition-all"
                    style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)" }}
                >
                    <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: "#FFFBEB" }}>
                        <Wrench size={18} style={{ color: "#D97706" }} />
                    </div>
                    <div className="flex-1">
                        <div style={{ fontSize: 14, fontWeight: 700, color: "#0F172A" }}>{it.storeName} · {it.equipmentName}</div>
                        <div style={{ fontSize: 12, color: "#94A3B8", marginTop: 2 }}>완료 {new Date(it.completedAt).toLocaleString()}</div>
                    </div>
                    <ChevronRight size={16} style={{ color: "#94A3B8" }} />
                </div>
            ))}
        </div>
    );
}