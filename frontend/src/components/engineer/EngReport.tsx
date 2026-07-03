import { useState, useEffect, useRef } from "react";
import { Plus, Trash2, CheckCircle, FileText } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { createReport, searchParts, type Part as MasterPart } from "../../api/report";

interface Part { code: string; name: string; qty: number; price: number }

interface Props {
  assignmentId?: number;
  asRequestId?: number;
  engineerId?: number;
  equipmentId?: number;
  onSubmit?: () => void;
}

export function EngReport({ assignmentId = 0, asRequestId = 0, engineerId = 0, equipmentId = 0, onSubmit }: Props) {
  const { accessToken } = useAuth();
  const draftKey = `report-draft:${assignmentId}`;

  const [parts, setParts] = useState<Part[]>(() => {
    try {
      const saved = localStorage.getItem(draftKey);
      if (saved) return JSON.parse(saved).parts;
    } catch {}
    return [{ code: "", name: "", qty: 1, price: 0 }];
  });
  const [labor, setLabor] = useState<string>(() => {
    try {
      const saved = localStorage.getItem(draftKey);
      if (saved) return JSON.parse(saved).labor;
    } catch {}
    return "0";
  });
  const [opinion, setOpinion] = useState<string>(() => {
    try {
      const saved = localStorage.getItem(draftKey);
      if (saved) return JSON.parse(saved).opinion;
    } catch {}
    return "";
  });
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeRow, setActiveRow] = useState<number | null>(null);
  const [suggestions, setSuggestions] = useState<MasterPart[]>([]);
  const searchTimer = useRef<number | undefined>(undefined);

  // 입력할 때마다 자동 저장
  useEffect(() => {
    try { localStorage.setItem(draftKey, JSON.stringify({ parts, labor, opinion })); } catch {}
  }, [parts, labor, opinion, draftKey]);

  const addPart = () => setParts((p) => [...p, { code: "", name: "", qty: 1, price: 0 }]);
  const removePart = (i: number) => setParts((p) => p.filter((_, idx) => idx !== i));
  const updatePart = (i: number, field: keyof Part, value: string | number) =>
      setParts((p) => p.map((part, idx) => idx === i ? { ...part, [field]: value } : part));

  const onPartInput = (i: number, field: "code" | "name", value: string) => {
    updatePart(i, field, value);
    setActiveRow(i);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    const kw = value.trim();
    if (!kw || !accessToken) { setSuggestions([]); return; }
    searchTimer.current = window.setTimeout(async () => {
      try { setSuggestions(await searchParts(accessToken, kw)); } catch { setSuggestions([]); }
    }, 200);
  };

  const pickPart = (i: number, part: MasterPart) => {
    setParts((p) => p.map((row, idx) => idx === i
      ? { ...row, code: part.partCode, name: part.name, price: part.unitPrice }
      : row));
    setSuggestions([]);
    setActiveRow(null);
  };

  const partsCost = parts.reduce((acc, p) => acc + p.qty * p.price, 0);
  const totalCost = partsCost + Number(labor);

  const handleSubmit = async () => {
    setError(null);
    if (!accessToken) { setError("로그인이 필요합니다."); return; }
    if (parts.some((p) => !p.code)) { setError("부품 코드를 모두 입력해주세요."); return; }
    setSubmitting(true);
    try {
      await createReport(accessToken, {
        assignmentId, asRequestId, engineerId,
        equipmentId: equipmentId || null,
        laborCost: Number(labor),
        diagnosis: opinion,
        parts: parts.map((p) => ({ partCode: p.code, quantity: p.qty, appliedPrice: p.price })),
      });
      try { localStorage.removeItem(draftKey); } catch {}   // ★ 제출 성공 시 draft 삭제
      setSubmitted(true);
      if (onSubmit) setTimeout(onSubmit, 1800);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? "리포트 작성에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="flex flex-col items-center justify-center h-80 gap-5">
        <div className="w-16 h-16 rounded-full flex items-center justify-center" style={{ background: "#F0FDF4" }}>
          <CheckCircle size={32} style={{ color: "#16A34A" }} />
        </div>
        <div className="text-center">
          <h2 style={{ color: "#0F172A" }}>정비 리포트 제출 완료</h2>
          <p style={{ fontSize: 13, color: "#64748B", marginTop: 6 }}>본사 검토 후 정산이 진행됩니다</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl flex flex-col gap-5">
      <div className="flex items-center gap-2 mb-1">
        <FileText size={16} style={{ color: "#64748B" }} />
        <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>정비 리포트 작성</h1>
      </div>

      {/* Parts */}
      <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
        <div className="flex items-center justify-between mb-4">
          <h3 style={{ color: "#0F172A" }}>교체 부품</h3>
          <button onClick={addPart} className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg" style={{ background: "#EFF6FF", color: "#2563EB", fontSize: 12, fontWeight: 600 }}>
            <Plus size={13} /> 부품 추가
          </button>
        </div>
        <div className="flex flex-col gap-3">
          {parts.map((p, i) => (
            <div key={i} className="relative grid grid-cols-12 gap-2 items-center">
              <input
                value={p.code}
                onChange={(e) => onPartInput(i, "code", e.target.value)}
                onFocus={() => setActiveRow(i)}
                onBlur={() => setTimeout(() => setActiveRow((r) => (r === i ? null : r)), 150)}
                placeholder="부품코드"
                className="col-span-3 px-3 py-2 rounded-lg"
                style={{ border: "1px solid rgba(15,23,42,0.1)", fontSize: 12, background: "#F8FAFC", fontFamily: "var(--font-mono)", outline: "none" }}
              />
              <input
                value={p.name}
                onChange={(e) => onPartInput(i, "name", e.target.value)}
                onFocus={() => setActiveRow(i)}
                onBlur={() => setTimeout(() => setActiveRow((r) => (r === i ? null : r)), 150)}
                placeholder="부품명"
                className="col-span-4 px-3 py-2 rounded-lg"
                style={{ border: "1px solid rgba(15,23,42,0.1)", fontSize: 12, background: "#F8FAFC", outline: "none" }}
              />
              <input
                type="number"
                value={p.qty}
                onChange={(e) => updatePart(i, "qty", Number(e.target.value))}
                className="col-span-1 px-2 py-2 rounded-lg text-center"
                style={{ border: "1px solid rgba(15,23,42,0.1)", fontSize: 12, background: "#F8FAFC", outline: "none" }}
              />
              <input
                type="number"
                value={p.price}
                onChange={(e) => updatePart(i, "price", Number(e.target.value))}
                placeholder="단가"
                className="col-span-3 px-3 py-2 rounded-lg"
                style={{ border: "1px solid rgba(15,23,42,0.1)", fontSize: 12, background: "#F8FAFC", outline: "none" }}
              />
              <button onClick={() => removePart(i)} className="col-span-1 flex items-center justify-center">
                <Trash2 size={14} style={{ color: "#EF4444" }} />
              </button>

              {activeRow === i && suggestions.length > 0 && (
                <div
                  className="absolute z-20 left-0 right-0 rounded-lg overflow-hidden"
                  style={{ top: "calc(100% + 4px)", background: "#fff", border: "1px solid rgba(15,23,42,0.12)", boxShadow: "0 8px 24px rgba(0,0,0,0.12)", maxHeight: 220, overflowY: "auto" }}
                >
                  {suggestions.map((s) => (
                    <button
                      key={s.id}
                      onMouseDown={(e) => { e.preventDefault(); pickPart(i, s); }}
                      className="w-full flex items-center gap-3 px-3 py-2 hover:bg-slate-50 transition-colors text-left"
                      style={{ borderBottom: "1px solid rgba(15,23,42,0.05)" }}
                    >
                      <span style={{ fontSize: 12, fontFamily: "var(--font-mono)", color: "#2563EB", minWidth: 96 }}>{s.partCode}</span>
                      <span style={{ fontSize: 12, color: "#0F172A", flex: 1 }}>{s.name}</span>
                      <span style={{ fontSize: 12, color: "#64748B" }}>{s.unitPrice.toLocaleString()}원</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Labor */}
      <div className="flex flex-col gap-2">
        <label style={{ fontSize: 13, fontWeight: 600, color: "#374151" }}>공임비 (원)</label>
        <input
          type="number"
          value={labor}
          onChange={(e) => setLabor(e.target.value)}
          className="px-4 py-3 rounded-xl"
          style={{ border: "1px solid rgba(15,23,42,0.1)", background: "#fff", fontSize: 13, outline: "none" }}
        />
      </div>

      {/* Opinion */}
      <div className="flex flex-col gap-2">
        <label style={{ fontSize: 13, fontWeight: 600, color: "#374151" }}>정비 의견</label>
        <textarea
          value={opinion}
          onChange={(e) => setOpinion(e.target.value)}
          rows={4}
          className="px-4 py-3 rounded-xl resize-none"
          style={{ border: "1px solid rgba(15,23,42,0.1)", background: "#fff", fontSize: 13, outline: "none", lineHeight: 1.6 }}
        />
      </div>

      {/* Summary */}
      <div className="rounded-xl p-4" style={{ background: "#F8FAFC", border: "1px solid rgba(15,23,42,0.08)" }}>
        <div className="flex justify-between mb-2">
          <span style={{ fontSize: 12, color: "#64748B" }}>부품비</span>
          <span style={{ fontSize: 12, fontWeight: 600, color: "#0F172A" }}>{partsCost.toLocaleString()}원</span>
        </div>
        <div className="flex justify-between mb-3">
          <span style={{ fontSize: 12, color: "#64748B" }}>공임비</span>
          <span style={{ fontSize: 12, fontWeight: 600, color: "#0F172A" }}>{Number(labor).toLocaleString()}원</span>
        </div>
        <div className="flex justify-between pt-3" style={{ borderTop: "1px solid rgba(15,23,42,0.08)" }}>
          <span style={{ fontSize: 13, fontWeight: 700, color: "#0F172A" }}>합계 (VAT 별도)</span>
          <span style={{ fontSize: 15, fontWeight: 800, color: "#2563EB" }}>{totalCost.toLocaleString()}원</span>
        </div>
      </div>

      {error && (
        <div className="rounded-xl px-4 py-3" style={{ background: "#FEF2F2", color: "#DC2626", fontSize: 13 }}>{error}</div>
      )}

      <button
        onClick={handleSubmit}
        disabled={submitting}
        className="w-full py-3.5 rounded-xl transition-all hover:opacity-90"
        style={{ background: "#2563EB", color: "#fff", fontSize: 14, fontWeight: 700, boxShadow: "0 4px 12px rgba(37,99,235,0.25)", opacity: submitting ? 0.6 : 1 }}
      >
        {submitting ? "제출 중..." : "정비 리포트 제출"}
      </button>
    </div>
  );
}
