import { useEffect, useState } from "react";
import { CheckCircle, XCircle, AlertTriangle, CreditCard } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { getSettlements, approveSettlement, rejectSettlement, type SettlementItem, type SettlementSummary } from "../../api/settlement";

type DisplayStatus = "PENDING" | "APPROVED" | "REJECTED" | "SUSPICIOUS";

const statusConfig: Record<DisplayStatus, { label: string; bg: string; color: string; border: string }> = {
  PENDING: { label: "검토 대기", bg: "#FFFBEB", color: "#D97706", border: "#FCD34D" },
  APPROVED: { label: "승인 완료", bg: "#F0FDF4", color: "#16A34A", border: "#86EFAC" },
  REJECTED: { label: "반려", bg: "#FEF2F2", color: "#DC2626", border: "#FCA5A5" },
  SUSPICIOUS: { label: "위변조 의심", bg: "#FFF7ED", color: "#EA580C", border: "#FDBA74" },
};

function displayStatus(item: SettlementItem): DisplayStatus {
  if (item.verificationStatus === "FLAGGED" && item.approvalStatus === "PENDING") return "SUSPICIOUS";
  if (item.approvalStatus === "PAID") return "APPROVED";
  return item.approvalStatus as DisplayStatus;
}

/**
 * 정산 관리 (피그마 AdminBilling 디자인 + 백엔드 /api/settlements 연결).
 * 긴급수수료 30% 모델 기준. (수수료 모델 변경 시 백엔드만 바꾸면 화면은 그대로 동작)
 */
export function AdminBilling() {
  const { accessToken } = useAuth();
  const [items, setItems] = useState<SettlementItem[]>([]);
  const [summary, setSummary] = useState<SettlementSummary>({ reviewPendingAmount: 0, approvedAmount: 0, flaggedCount: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!accessToken) return;
    getSettlements(accessToken)
      .then((d) => { setItems(d.items); setSummary(d.summary); })
      .finally(() => setLoading(false));
  }, [accessToken]);

  async function refresh() {
    if (!accessToken) return;
    const d = await getSettlements(accessToken);
    setItems(d.items); setSummary(d.summary);
  }

  async function approve(id: number) {
    if (!accessToken) return;
    await approveSettlement(accessToken, id);
    await refresh();
  }
  async function reject(id: number) {
    if (!accessToken) return;
    await rejectSettlement(accessToken, id);
    await refresh();
  }

  if (loading) return <div style={{ color: "#64748B", padding: 24 }}>불러오는 중...</div>;

  return (
    <div className="flex flex-col gap-5">
      <div>
        <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>정산 관리</h1>
        <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>긴급수수료 30% 모델 · 위변조 자동 검증</p>
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-3 gap-4">
        <div className="rounded-xl p-4 flex items-center gap-3" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "#FFFBEB" }}>
            <CreditCard size={16} style={{ color: "#D97706" }} />
          </div>
          <div>
            <div style={{ fontSize: 11, color: "#94A3B8" }}>검토 대기</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: "#0F172A" }}>{summary.reviewPendingAmount.toLocaleString()}원</div>
          </div>
        </div>
        <div className="rounded-xl p-4 flex items-center gap-3" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "#F0FDF4" }}>
            <CheckCircle size={16} style={{ color: "#16A34A" }} />
          </div>
          <div>
            <div style={{ fontSize: 11, color: "#94A3B8" }}>승인 완료</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: "#0F172A" }}>{summary.approvedAmount.toLocaleString()}원</div>
          </div>
        </div>
        <div className="rounded-xl p-4 flex items-center gap-3" style={{ background: "#FFF7ED", border: "1px solid #FDBA74" }}>
          <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "#FED7AA" }}>
            <AlertTriangle size={16} style={{ color: "#EA580C" }} />
          </div>
          <div>
            <div style={{ fontSize: 11, color: "#92400E" }}>위변조 의심 건</div>
            <div style={{ fontSize: 18, fontWeight: 700, color: "#EA580C" }}>{summary.flaggedCount}건</div>
          </div>
        </div>
      </div>

      {/* 정산 테이블 */}
      <div className="rounded-xl overflow-hidden" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
        <table className="w-full">
          <thead>
            <tr style={{ background: "#F8FAFC" }}>
              {["청구 ID", "엔지니어", "A/S 번호", "부품비", "공임비", "긴급수수료", "합계", "상태", "조치"].map((h) => (
                <th key={h} style={{ fontSize: 11, color: "#64748B", fontWeight: 600, padding: "12px 16px", textAlign: "left", borderBottom: "1px solid rgba(15,23,42,0.06)", whiteSpace: "nowrap" }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((b, i) => {
              const ds = displayStatus(b);
              const sc = statusConfig[ds];
              return (
                <tr key={b.id} className="hover:bg-slate-50 transition-colors" style={{ borderBottom: i < items.length - 1 ? "1px solid rgba(15,23,42,0.04)" : "none" }}>
                  <td style={{ padding: "12px 16px", fontSize: 12, fontFamily: "var(--font-mono)", color: "#475569" }}>{`BL-${String(b.id).padStart(4, "0")}`}</td>
                  <td style={{ padding: "12px 16px", fontSize: 13, color: "#0F172A", fontWeight: 500 }}>{`엔지니어 #${b.engineerId}`}</td>
                  <td style={{ padding: "12px 16px", fontSize: 12, fontFamily: "var(--font-mono)", color: "#64748B" }}>{b.invoiceNumber}</td>
                  <td style={{ padding: "12px 16px", fontSize: 12, color: "#374151" }}>{b.partsCost.toLocaleString()}</td>
                  <td style={{ padding: "12px 16px", fontSize: 12, color: "#374151" }}>{b.laborCost.toLocaleString()}</td>
                  <td style={{ padding: "12px 16px", fontSize: 12, color: b.emergencyFee > 0 ? "#D97706" : "#94A3B8" }}>
                    {b.emergencyFee > 0 ? b.emergencyFee.toLocaleString() : "-"}
                  </td>
                  <td style={{ padding: "12px 16px", fontSize: 13, fontWeight: 700, color: "#0F172A" }}>{b.billedAmount.toLocaleString()}</td>
                  <td style={{ padding: "12px 16px" }}>
                    <div className="flex items-center gap-1">
                      {ds === "SUSPICIOUS" && <AlertTriangle size={12} style={{ color: "#EA580C" }} />}
                      <span className="px-2 py-0.5 rounded-full" style={{ background: sc.bg, color: sc.color, fontSize: 11, fontWeight: 600, border: `1px solid ${sc.border}` }}>
                        {sc.label}
                      </span>
                    </div>
                  </td>
                  <td style={{ padding: "12px 16px" }}>
                    {b.approvalStatus === "PENDING" && (
                      <div className="flex gap-1.5">
                        <button onClick={() => approve(b.id)} className="flex items-center gap-1 px-2.5 py-1 rounded-lg" style={{ background: "#F0FDF4", color: "#16A34A", fontSize: 11, fontWeight: 600 }}>
                          <CheckCircle size={11} /> 승인
                        </button>
                        <button onClick={() => reject(b.id)} className="flex items-center gap-1 px-2.5 py-1 rounded-lg" style={{ background: "#FEF2F2", color: "#DC2626", fontSize: 11, fontWeight: 600 }}>
                          <XCircle size={11} /> 반려
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
