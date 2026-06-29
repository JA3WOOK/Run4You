import { useEffect, useState, type ReactNode } from "react";
import { Activity, AlertTriangle, ShieldCheck, Wrench, Coins } from "lucide-react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
  PieChart, Pie, Legend,
} from "recharts";
import { useAuth } from "../../context/AuthContext";
import { getDashboard, type DashboardResponse } from "../../api/dashboard";

const GRADE_COLORS = ["#16A34A", "#2563EB", "#D97706", "#DC2626"]; // A B C D
const CAT_COLOR = "#2563EB";

function KpiCard({ title, value, sub, icon, accent }: { title: string; value: string; sub: string; icon: ReactNode; accent: string }) {
  return (
    <div className="rounded-xl p-4" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
      <div className="flex items-center justify-between mb-2">
        <span style={{ fontSize: 12, color: "#64748B" }}>{title}</span>
        <div className="w-7 h-7 rounded-lg flex items-center justify-center" style={{ background: `${accent}14`, color: accent }}>{icon}</div>
      </div>
      <div style={{ fontSize: 22, fontWeight: 800, color: "#0F172A", letterSpacing: "-0.02em" }}>{value}</div>
      <div style={{ fontSize: 11, color: "#94A3B8", marginTop: 2 }}>{sub}</div>
    </div>
  );
}

/**
 * 결함·MTBF·등급 통계 대시보드 (민구 담당 데이터).
 * /api/dashboard 의 정산요약·결함통계·MTBF·등급분포·엔지니어통계를 시각화한다.
 */
export function AdminStats() {
  const { accessToken } = useAuth();
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!accessToken) return;
    getDashboard(accessToken).then(setData).finally(() => setLoading(false));
  }, [accessToken]);

  if (loading) return <div style={{ color: "#64748B", padding: 24 }}>불러오는 중...</div>;
  if (!data) return <div style={{ color: "#DC2626", padding: 24 }}>데이터를 불러오지 못했습니다.</div>;

  const won = (n: number) => `${n.toLocaleString()}원`;

  const gradeData = [
    { name: "A (양호)", value: data.gradeDistribution.a },
    { name: "B (주의)", value: data.gradeDistribution.b },
    { name: "C (경고)", value: data.gradeDistribution.c },
    { name: "D (교체)", value: data.gradeDistribution.d },
  ];
  const defectData = data.defectsByCategory.map((d) => ({ name: d.category, value: d.replacedQuantity }));
  const mtbfData = data.mtbf
    .filter((m) => m.mtbfDays != null)
    .map((m) => ({ name: `기자재 #${m.equipmentId}`, value: m.mtbfDays as number }));
  const engData = data.engineerStats.map((e) => ({ name: `#${e.engineerId}`, value: e.repairCount }));

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>결함·MTBF·정산 통계</h1>
        <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>수리 데이터 기반 B2B 인텔리전스</p>
      </div>

      {/* KPI */}
      <div className="grid grid-cols-4 gap-4">
        <KpiCard title="총 수리 건수" value={`${data.repair.totalReports}`} sub="누적 정비 리포트" icon={<Wrench size={15} />} accent="#2563EB" />
        <KpiCard title="총 정산 청구액" value={won(data.settlement.totalBilled)} sub={`정산 ${data.settlement.totalCount}건`} icon={<Coins size={15} />} accent="#16A34A" />
        <KpiCard title="위변조 의심" value={`${data.settlement.flaggedCount}건`} sub="단가 불일치 자동 탐지" icon={<AlertTriangle size={15} />} accent="#EA580C" />
        <KpiCard title="전체 평균 MTBF" value={data.overallMtbfDays != null ? `${data.overallMtbfDays}일` : "—"} sub="평균 고장 간격" icon={<Activity size={15} />} accent="#7C3AED" />
      </div>

      <div className="grid grid-cols-3 gap-5">
        {/* 결함 통계 (카테고리별) */}
        <div className="col-span-2 rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="flex items-center gap-2 mb-1">
            <AlertTriangle size={14} style={{ color: "#D97706" }} />
            <h3 style={{ color: "#0F172A" }}>카테고리별 결함(교체) 통계</h3>
          </div>
          <p style={{ fontSize: 11, color: "#94A3B8", marginBottom: 16 }}>교체 부품 수량 기준</p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={defectData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 10, fill: "#94A3B8" }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip contentStyle={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", borderRadius: 8, fontSize: 12 }} formatter={(v: number) => [`${v}개`, "교체"]} />
              <Bar dataKey="value" fill={CAT_COLOR} radius={[4, 4, 0, 0]} barSize={40} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* 등급 분포 (도넛) */}
        <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="flex items-center gap-2 mb-1">
            <ShieldCheck size={14} style={{ color: "#16A34A" }} />
            <h3 style={{ color: "#0F172A" }}>진단서 등급 분포</h3>
          </div>
          <p style={{ fontSize: 11, color: "#94A3B8", marginBottom: 8 }}>평균 건강점수 {data.gradeDistribution.avgScore}점</p>
          <ResponsiveContainer width="100%" height={210}>
            <PieChart>
              <Pie data={gradeData} dataKey="value" nameKey="name" innerRadius={45} outerRadius={75} paddingAngle={2}>
                {gradeData.map((_, i) => <Cell key={i} fill={GRADE_COLORS[i]} />)}
              </Pie>
              <Tooltip contentStyle={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", borderRadius: 8, fontSize: 12 }} formatter={(v: number) => [`${v}건`, ""]} />
              <Legend wrapperStyle={{ fontSize: 11 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-5">
        {/* MTBF (기자재별) */}
        <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="flex items-center gap-2 mb-1">
            <Activity size={14} style={{ color: "#7C3AED" }} />
            <h3 style={{ color: "#0F172A" }}>기자재별 MTBF</h3>
          </div>
          <p style={{ fontSize: 11, color: "#94A3B8", marginBottom: 16 }}>평균 고장 간격 (일) · 고장 2회 이상</p>
          {mtbfData.length === 0 ? (
            <div style={{ fontSize: 12, color: "#94A3B8", padding: "20px 0" }}>고장 2회 이상인 기자재가 없습니다.</div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={mtbfData} layout="vertical" barSize={14}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 10, fill: "#94A3B8" }} axisLine={false} tickLine={false} />
                <YAxis dataKey="name" type="category" tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} width={80} />
                <Tooltip contentStyle={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", borderRadius: 8, fontSize: 12 }} formatter={(v: number) => [`${v}일`, "MTBF"]} />
                <Bar dataKey="value" fill="#7C3AED" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* 엔지니어별 수리 건수 */}
        <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <div className="flex items-center gap-2 mb-1">
            <Wrench size={14} style={{ color: "#2563EB" }} />
            <h3 style={{ color: "#0F172A" }}>엔지니어별 수리 건수</h3>
          </div>
          <p style={{ fontSize: 11, color: "#94A3B8", marginBottom: 16 }}>누적 처리 건수</p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={engData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#64748B" }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 10, fill: "#94A3B8" }} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip contentStyle={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", borderRadius: 8, fontSize: 12 }} formatter={(v: number) => [`${v}건`, "수리"]} />
              <Bar dataKey="value" fill="#2563EB" radius={[4, 4, 0, 0]} barSize={32} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
