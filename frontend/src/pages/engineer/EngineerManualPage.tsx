import { useState } from "react";
import { useManuals } from "../../hooks/useManuals";
import type { Manual, ManualType } from "../../api/education";

interface Props {
  accessToken: string;
}

const TABS: { label: string; value?: ManualType }[] = [
  { label: "전체", value: undefined },
  { label: "긴급 출동 매뉴얼", value: "DISPATCH_GUIDE" },
  { label: "증상별 대응 매뉴얼", value: "SYMPTOM_GUIDE" },
];

// 매뉴얼 종류/증상 태그 pill 색상 — Sidebar 역할 뱃지와 동일한 방식
const TYPE_ACCENT: Record<ManualType, string> = {
  DISPATCH_GUIDE: "#F59E0B",
  SYMPTOM_GUIDE: "#378ADD",
};

const TYPE_LABEL: Record<ManualType, string> = {
  DISPATCH_GUIDE: "긴급 출동",
  SYMPTOM_GUIDE: "증상 대응",
};

function ManualTag({ manual }: { manual: Manual }) {
  const label = manual.faultCategory ?? TYPE_LABEL[manual.manualType];
  const accent = TYPE_ACCENT[manual.manualType];
  return (
    <span
      style={{
        display: "inline-block",
        fontSize: 12,
        fontWeight: 700,
        padding: "3px 10px",
        borderRadius: 20,
        background: `${accent}22`,
        color: accent,
        border: `1px solid ${accent}55`,
        whiteSpace: "nowrap",
      }}
    >
      {label}
    </span>
  );
}

export default function EngineerManualPage({ accessToken }: Props) {
  const [manualType, setManualType] = useState<ManualType | undefined>(undefined);
  const { manuals, loading, error } = useManuals(accessToken, manualType);
  const [openId, setOpenId] = useState<number | null>(null);

  return (
    <div>
      <h1 style={{ fontSize: 20, color: "#0F172A", marginBottom: 20, fontWeight: 700 }}>매뉴얼</h1>

      <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
        {TABS.map((tab) => (
          <button
            key={tab.label}
            onClick={() => setManualType(tab.value)}
            style={{
              padding: "6px 14px",
              borderRadius: 20,
              border: manualType === tab.value ? "1px solid var(--primary)" : "1px solid var(--border)",
              background: manualType === tab.value ? "var(--primary)" : "transparent",
              color: manualType === tab.value ? "#fff" : "#94A3B8",
              cursor: "pointer",
              fontSize: 13,
              fontWeight: manualType === tab.value ? 600 : 400,
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading && <p style={{ color: "#94A3B8" }}>불러오는 중...</p>}
      {error && <p style={{ color: "#F87171" }}>{error}</p>}

      {manuals.map((m) => {
        const open = openId === m.manualId;
        return (
          <div
            key={m.manualId}
            className="rounded-xl"
            style={{
              background: "var(--card)",
              border: "1px solid var(--border)",
              padding: "1rem 1.25rem",
              marginBottom: 10,
              cursor: "pointer",
            }}
            onClick={() => setOpenId(open ? null : m.manualId)}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
              <h3 style={{ margin: 0, fontSize: 15, color: "#0F172A", fontWeight: 600 }}>{m.title}</h3>
              <ManualTag manual={m} />
            </div>
            {open && (
              <div style={{ marginTop: 10, fontSize: 13, color: "#0F172A", lineHeight: 1.7, whiteSpace: "pre-line" }}>
                {m.content}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
