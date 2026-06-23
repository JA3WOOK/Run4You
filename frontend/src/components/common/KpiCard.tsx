interface KpiCardProps {
    title: string;
    value: string | number;
    sub?: string;
    icon: React.ReactNode;
    trend?: { value: string; up: boolean };
    accent?: string;
}

export function KpiCard({ title, value, sub, icon, trend, accent = "#2563EB" }: KpiCardProps) {
    return (
        <div
            className="rounded-xl p-5 flex flex-col gap-3"
            style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}
        >
            <div className="flex items-start justify-between">
                <div>
                    <div style={{ fontSize: 12, color: "#64748B", fontWeight: 500, marginBottom: 4 }}>{title}</div>
                    <div style={{ fontSize: 26, fontWeight: 700, color: "#0F172A", letterSpacing: "-0.02em", lineHeight: 1.1 }}>{value}</div>
                    {sub && <div style={{ fontSize: 11, color: "#94A3B8", marginTop: 2 }}>{sub}</div>}
                </div>
                <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ background: `${accent}14` }}>
                    <span style={{ color: accent }}>{icon}</span>
                </div>
            </div>
            {trend && (
                <div className="flex items-center gap-1.5">
          <span style={{ fontSize: 11, color: trend.up ? "#16A34A" : "#DC2626", fontWeight: 600 }}>
            {trend.up ? "▲" : "▼"} {trend.value}
          </span>
                    <span style={{ fontSize: 11, color: "#94A3B8" }}>전주 대비</span>
                </div>
            )}
        </div>
    );
}