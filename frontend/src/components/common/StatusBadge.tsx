type StatusType = "OPERATIONAL" | "FAULTY" | "REPAIRING" | "EMERGENCY" | "NORMAL" | "PENDING" | "COMPLETED";

const config: Record<StatusType, { label: string; bg: string; color: string; dot: string }> = {
    OPERATIONAL: { label: "정상", bg: "#F0FDF4", color: "#16A34A", dot: "#16A34A" },
    FAULTY:      { label: "고장", bg: "#FEF2F2", color: "#DC2626", dot: "#DC2626" },
    REPAIRING:   { label: "수리중", bg: "#FFFBEB", color: "#D97706", dot: "#D97706" },
    EMERGENCY:   { label: "긴급", bg: "#FEF2F2", color: "#DC2626", dot: "#DC2626" },
    NORMAL:      { label: "일반", bg: "#EFF6FF", color: "#2563EB", dot: "#2563EB" },
    PENDING:     { label: "대기", bg: "#F1F5F9", color: "#64748B", dot: "#94A3B8" },
    COMPLETED:   { label: "완료", bg: "#F0FDF4", color: "#16A34A", dot: "#16A34A" },
};

interface StatusBadgeProps {
    status: StatusType;
    size?: "sm" | "md";
}

export function StatusBadge({ status, size = "md" }: StatusBadgeProps) {
    const c = config[status];
    return (
        <span
            className="inline-flex items-center gap-1 rounded-full"
            style={{
                background: c.bg,
                color: c.color,
                fontSize: size === "sm" ? 12 : 13,
                fontWeight: 700,
                padding: size === "sm" ? "4px 10px" : "5px 12px",
                border: `1px solid ${c.color}30`,
            }}
        >
      <span className="w-1.5 h-1.5 rounded-full" style={{ background: c.dot }} />
            {c.label}
    </span>
    );
}