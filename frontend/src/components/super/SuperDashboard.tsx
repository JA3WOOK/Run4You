import { useEffect, useState } from 'react';
import { AlertCircle, Users, Wrench, TrendingUp, CheckCircle, XCircle, Building2, Wifi } from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
  LineChart, Line,
} from 'recharts';
import { useAuth } from '../../context/AuthContext';
import { getSuperDashboard, type SuperDashboardData } from '../../api/superDashboard';
import { approveBrand, rejectBrand, approveUser, rejectUser, updateCommissionRate } from '../../api/admin';

const roleLabel: Record<string, string> = {
  STORE_OWNER: '점주',
  ENGINEER: '엔지니어',
  BRAND_ADMIN: '본사 관리자',
};

const statusLabel: Record<string, string> = { PENDING: '대기', ACTIVE: '승인', INACTIVE: '거절' };
const statusColor: Record<string, string> = { PENDING: '#D97706', ACTIVE: '#16A34A', INACTIVE: '#DC2626' };
const CHART_COLORS = ['#7C3AED', '#2563EB', '#16A34A', '#D97706', '#DC2626', '#0891B2'];

function KpiCard({ title, value, sub, icon, accent }: {
  title: string; value: string; sub: string; icon: React.ReactNode; accent: string;
}) {
  return (
    <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
      <div className="flex items-center justify-between mb-3">
        <span style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>{title}</span>
        <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: `${accent}18`, color: accent }}>
          {icon}
        </div>
      </div>
      <div style={{ fontSize: 26, fontWeight: 800, color: 'var(--foreground)', letterSpacing: '-0.02em' }}>{value}</div>
      <div style={{ fontSize: 12, color: 'var(--muted-foreground)', marginTop: 4 }}>{sub}</div>
    </div>
  );
}

export function SuperDashboard() {
  const { accessToken } = useAuth();
  const [data, setData] = useState<SuperDashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [editingBrandId, setEditingBrandId] = useState<number | null>(null);
  const [editRate, setEditRate] = useState('');

  const reload = () => {
    if (!accessToken) return;
    setLoading(true);
    getSuperDashboard(accessToken).then(setData).finally(() => setLoading(false));
  };

  useEffect(() => { reload(); }, [accessToken]);

  async function handleApproveBrand(id: number) {
    if (!accessToken) return;
    await approveBrand(accessToken, id);
    reload();
  }
  async function handleRejectBrand(id: number) {
    if (!accessToken) return;
    await rejectBrand(accessToken, id);
    reload();
  }
  async function handleApproveUser(id: number) {
    if (!accessToken) return;
    await approveUser(accessToken, id);
    reload();
  }
  async function handleRejectUser(id: number) {
    if (!accessToken) return;
    await rejectUser(accessToken, id);
    reload();
  }
  async function handleUpdateRate(id: number) {
    if (!accessToken || !editRate) return;
    await updateCommissionRate(accessToken, id, Number(editRate));
    setEditingBrandId(null);
    setEditRate('');
    reload();
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;
  if (!data) return <div style={{ color: 'var(--destructive)', padding: 32 }}>데이터를 불러오지 못했습니다.</div>;

  const won = (n: number) => `${n.toLocaleString()}원`;
  const hasPending = data.pendingBrands.length > 0 || data.pendingUsers.length > 0;
  const completionRate = data.kpi.totalAsRequests > 0
    ? Math.round(data.brandStats.reduce((s, b) => s + b.completedAsCount, 0) / data.kpi.totalAsRequests * 100)
    : 0;

  return (
    <div className="flex flex-col gap-6">
      {/* 헤더 + SSE 상태 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', letterSpacing: '-0.02em' }}>플랫폼 총괄 대시보드</h1>
          <p style={{ fontSize: 13, color: 'var(--muted-foreground)', marginTop: 2 }}>승인 대기 처리 및 플랫폼 전체 현황을 확인하세요.</p>
        </div>
        <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full" style={{ background: '#16A34A18', border: '1px solid #16A34A30' }}>
          <Wifi size={13} style={{ color: '#16A34A' }} />
          <span style={{ fontSize: 12, color: '#16A34A', fontWeight: 600 }}>SSE 연결됨</span>
        </div>
      </div>

      {/* KPI */}
      <div className="grid grid-cols-4 gap-4">
        <KpiCard title="플랫폼 총 A/S" value={`${data.kpi.totalAsRequests}건`} sub="누적 접수" icon={<Wrench size={16} />} accent="#7C3AED" />
        <KpiCard
          title="승인 대기"
          value={`${data.kpi.pendingApprovals}건`}
          sub={`브랜드 ${data.pendingBrands.length} · 회원 ${data.pendingUsers.length}`}
          icon={<AlertCircle size={16} />}
          accent={data.kpi.pendingApprovals > 0 ? '#D97706' : '#16A34A'}
        />
        <KpiCard title="활성 엔지니어" value={`${data.kpi.activeEngineers}명`} sub="현재 승인된 엔지니어" icon={<Users size={16} />} accent="#2563EB" />
        <KpiCard title="이번 달 청구액" value={won(data.kpi.thisMonthCommission)} sub={`전체 완료율 ${completionRate}%`} icon={<TrendingUp size={16} />} accent="#16A34A" />
      </div>

      {/* 차트 2개 */}
      <div className="grid grid-cols-2 gap-5">
        {/* 브랜드별 A/S 건수 바차트 */}
        <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
          <h3 style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)', marginBottom: 4 }}>브랜드별 A/S 건수</h3>
          <p style={{ fontSize: 11, color: 'var(--muted-foreground)', marginBottom: 16 }}>총 접수 기준</p>
          {data.brandStats.filter(b => b.totalAsCount > 0).length === 0 ? (
            <div style={{ fontSize: 12, color: 'var(--muted-foreground)', padding: '20px 0' }}>데이터가 없습니다.</div>
          ) : (
            <ResponsiveContainer width="100%" height={Math.max(200, data.brandStats.filter(b => b.status === 'ACTIVE').length * 56)}>
              <BarChart data={data.brandStats.filter(b => b.status === 'ACTIVE')} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 10, fill: '#94A3B8' }} axisLine={false} tickLine={false} allowDecimals={false} />
                <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: '#64748B' }} axisLine={false} tickLine={false} width={90} />
                <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} formatter={(v: number) => [`${v}건`, 'A/S']} />
                <Bar dataKey="totalAsCount" fill="#7C3AED" radius={[0, 4, 4, 0]} barSize={14} name="총 접수" />
                <Bar dataKey="completedAsCount" fill="#16A34A" radius={[0, 4, 4, 0]} barSize={14} name="완료" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* 카테고리별 도넛차트 */}
        <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
          <h3 style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)', marginBottom: 4 }}>고장 카테고리별 비율</h3>
          <p style={{ fontSize: 11, color: 'var(--muted-foreground)', marginBottom: 8 }}>전체 A/S 접수 기준</p>
          {data.categoryStats.length === 0 ? (
            <div style={{ fontSize: 12, color: 'var(--muted-foreground)', padding: '20px 0' }}>데이터가 없습니다.</div>
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={data.categoryStats} dataKey="count" nameKey="category" innerRadius={50} outerRadius={80} paddingAngle={2}>
                  {data.categoryStats.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                </Pie>
                <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} formatter={(v: number) => [`${v}건`, '']} />
                <Legend wrapperStyle={{ fontSize: 11 }} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* 월별 수수료 라인차트 */}
      <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
        <h3 style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)', marginBottom: 4 }}>수수료 수익 현황 (최근 6개월)</h3>
        <p style={{ fontSize: 11, color: 'var(--muted-foreground)', marginBottom: 16 }}>월별 청구액 합계</p>
        <ResponsiveContainer width="100%" height={180}>
          <LineChart data={data.monthlyCommission}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" vertical={false} />
            <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#64748B' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 10, fill: '#94A3B8' }} axisLine={false} tickLine={false}
              tickFormatter={(v: number) => v >= 1000000 ? `${(v / 1000000).toFixed(1)}M` : `${v}`} />
            <Tooltip contentStyle={{ fontSize: 12, borderRadius: 8 }} formatter={(v: number) => [won(v), '청구액']} />
            <Line type="monotone" dataKey="amount" stroke="#7C3AED" strokeWidth={2.5} dot={{ fill: '#7C3AED', r: 4 }} activeDot={{ r: 6 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* 승인 대기 빠른 처리 */}
      {hasPending && (
        <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
          <div className="flex items-center gap-2 mb-4">
            <AlertCircle size={16} style={{ color: '#D97706' }} />
            <h2 style={{ fontSize: 15, fontWeight: 700, color: 'var(--foreground)' }}>승인 대기 ({data.kpi.pendingApprovals}건)</h2>
          </div>
          <div className="flex flex-col gap-2">
            {data.pendingBrands.map(b => (
              <div key={`brand-${b.id}`} className="flex items-center gap-4 px-4 py-3 rounded-lg" style={{ background: 'var(--muted)' }}>
                <Building2 size={15} style={{ color: '#7C3AED', flexShrink: 0 }} />
                <div className="flex-1 min-w-0">
                  <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)' }}>{b.name}</span>
                  <span className="ml-2 px-1.5 py-0.5 rounded text-xs" style={{ background: '#7C3AED18', color: '#7C3AED' }}>브랜드</span>
                  <div style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>사업자번호 {b.businessNo} · 수수료 {b.commissionRate}%</div>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button onClick={() => handleApproveBrand(b.id)} className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                    style={{ background: '#16A34A', color: '#fff', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
                    <CheckCircle size={13} /> 승인
                  </button>
                  <button onClick={() => handleRejectBrand(b.id)} className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                    style={{ background: '#DC2626', color: '#fff', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
                    <XCircle size={13} /> 거절
                  </button>
                </div>
              </div>
            ))}
            {data.pendingUsers.map(u => (
              <div key={`user-${u.id}`} className="flex items-center gap-4 px-4 py-3 rounded-lg" style={{ background: 'var(--muted)' }}>
                <Users size={15} style={{ color: '#2563EB', flexShrink: 0 }} />
                <div className="flex-1 min-w-0">
                  <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)' }}>{u.name}</span>
                  <span className="ml-2 px-1.5 py-0.5 rounded text-xs" style={{ background: '#2563EB18', color: '#2563EB' }}>{roleLabel[u.role] ?? u.role}</span>
                  <div style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>{u.email}</div>
                </div>
                <div className="flex gap-2 shrink-0">
                  <button onClick={() => handleApproveUser(u.id)} className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                    style={{ background: '#16A34A', color: '#fff', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
                    <CheckCircle size={13} /> 승인
                  </button>
                  <button onClick={() => handleRejectUser(u.id)} className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                    style={{ background: '#DC2626', color: '#fff', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
                    <XCircle size={13} /> 거절
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      {!hasPending && (
        <div className="rounded-xl px-5 py-4 flex items-center gap-3" style={{ background: '#16A34A10', border: '1px solid #16A34A30' }}>
          <CheckCircle size={16} style={{ color: '#16A34A' }} />
          <span style={{ fontSize: 14, color: '#16A34A', fontWeight: 500 }}>승인 대기 항목이 없습니다.</span>
        </div>
      )}

      {/* 브랜드별 성과 테이블 */}
      <div className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
        <div className="flex items-center gap-2 mb-4">
          <Building2 size={16} style={{ color: '#7C3AED' }} />
          <h2 style={{ fontSize: 15, fontWeight: 700, color: 'var(--foreground)' }}>브랜드 성과 지표</h2>
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)' }}>
                {['브랜드명', '상태', '수수료율', 'A/S 건수', '완료율', '평균 처리시간', '총 청구액'].map(h => (
                  <th key={h} style={{ padding: '8px 12px', textAlign: 'left', color: 'var(--muted-foreground)', fontWeight: 500 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.brandStats.map(b => {
                const rate = b.totalAsCount > 0 ? Math.round(b.completedAsCount / b.totalAsCount * 100) : 0;
                return (
                  <tr key={b.id} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '12px', fontWeight: 600, color: 'var(--foreground)' }}>{b.name}</td>
                    <td style={{ padding: '12px' }}>
                      <span className="px-2 py-0.5 rounded-full" style={{ fontSize: 12, fontWeight: 600, background: `${statusColor[b.status]}18`, color: statusColor[b.status] }}>
                        {statusLabel[b.status] ?? b.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px', color: 'var(--foreground)' }}>
                      {editingBrandId === b.id ? (
                        <div className="flex items-center gap-2">
                          <input type="number" value={editRate} onChange={e => setEditRate(e.target.value)}
                            className="px-2 py-1 rounded-lg outline-none w-16"
                            style={{ background: 'var(--muted)', border: '1px solid var(--border)', fontSize: 13 }} />
                          <span style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>%</span>
                          <button onClick={() => handleUpdateRate(b.id)} className="px-2 py-1 rounded-lg"
                            style={{ background: '#7C3AED', color: '#fff', fontSize: 12, cursor: 'pointer' }}>저장</button>
                          <button onClick={() => setEditingBrandId(null)} className="px-2 py-1 rounded-lg"
                            style={{ background: 'var(--muted)', color: 'var(--muted-foreground)', fontSize: 12, cursor: 'pointer' }}>취소</button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-2">
                          <span>{b.commissionRate}%</span>
                          {b.status === 'ACTIVE' && (
                            <button onClick={() => { setEditingBrandId(b.id); setEditRate(String(b.commissionRate)); }}
                              style={{ fontSize: 11, color: '#7C3AED', cursor: 'pointer', background: 'none', border: 'none' }}>수정</button>
                          )}
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px', color: 'var(--foreground)' }}>{b.totalAsCount}건</td>
                    <td style={{ padding: '12px' }}>
                      <div className="flex items-center gap-2">
                        <div style={{ width: 60, height: 6, borderRadius: 3, background: 'var(--muted)', overflow: 'hidden' }}>
                          <div style={{ width: `${rate}%`, height: '100%', background: rate >= 70 ? '#16A34A' : rate >= 40 ? '#D97706' : '#DC2626', borderRadius: 3 }} />
                        </div>
                        <span style={{ fontSize: 12, color: 'var(--foreground)' }}>{rate}%</span>
                      </div>
                    </td>
                    <td style={{ padding: '12px', color: 'var(--foreground)' }}>
                      {b.avgProcessingHours != null ? `${b.avgProcessingHours.toFixed(1)}h` : '—'}
                    </td>
                    <td style={{ padding: '12px', fontWeight: 500, color: 'var(--foreground)' }}>{won(b.totalBilled)}</td>
                  </tr>
                );
              })}
              {data.brandStats.length === 0 && (
                <tr><td colSpan={7} style={{ padding: '20px 12px', color: 'var(--muted-foreground)', textAlign: 'center' }}>등록된 브랜드가 없습니다.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
