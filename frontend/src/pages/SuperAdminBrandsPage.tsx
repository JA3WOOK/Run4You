import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getBrands, approveBrand, rejectBrand, updateCommissionRate, deleteBrand, type Brand } from '../api/admin';

const statusLabel: Record<string, string> = {
  PENDING: '승인 대기',
  ACTIVE: '승인',
  INACTIVE: '거절',
};

const statusColor: Record<string, string> = {
  PENDING: '#D97706',
  ACTIVE: '#16A34A',
  INACTIVE: '#DC2626',
};

export default function SuperAdminBrandsPage() {
  const { accessToken } = useAuth();
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editRate, setEditRate] = useState('');

  useEffect(() => {
    if (!accessToken) return;
    getBrands(accessToken).then(setBrands).finally(() => setLoading(false));
  }, [accessToken]);

  async function handleApprove(id: number) {
    if (!accessToken) return;
    const updated = await approveBrand(accessToken, id);
    setBrands(prev => prev.map(b => b.id === id ? updated : b));
  }

  async function handleReject(id: number) {
    if (!accessToken) return;
    await rejectBrand(accessToken, id);
    setBrands(prev => prev.filter(b => b.id !== id));
  }

  async function handleDelete(id: number) {
    if (!accessToken) return;
    if (!confirm('정말 삭제하시겠습니까? 연결된 관리자 계정도 함께 삭제됩니다.')) return;
    await deleteBrand(accessToken, id);
    setBrands(prev => prev.filter(b => b.id !== id));
  }

  async function handleUpdateRate(id: number) {
    if (!accessToken || !editRate) return;
    const updated = await updateCommissionRate(accessToken, id, Number(editRate));
    setBrands(prev => prev.map(b => b.id === id ? updated : b));
    setEditingId(null);
    setEditRate('');
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;

  return (
    <div>
      <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', marginBottom: 20 }}>브랜드 관리</h2>

      {brands.length === 0 ? (
        <div style={{ color: 'var(--muted-foreground)', fontSize: 14 }}>등록된 브랜드가 없습니다.</div>
      ) : (
        <div className="flex flex-col gap-3">
          {brands.map(brand => (
            <div key={brand.id} className="rounded-xl p-5 flex items-center gap-6"
              style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>

              {/* 브랜드 정보 */}
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span style={{ fontSize: 16, fontWeight: 600, color: 'var(--foreground)' }}>{brand.name}</span>
                  <span className="px-2 py-0.5 rounded-full" style={{
                    fontSize: 12, fontWeight: 600,
                    background: `${statusColor[brand.status]}18`,
                    color: statusColor[brand.status],
                  }}>
                    {statusLabel[brand.status]}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>사업자 번호: {brand.businessNo}</div>
              </div>

              {/* 수수료율 */}
              <div className="flex items-center gap-2">
                {editingId === brand.id ? (
                  <>
                    <input
                      type="number"
                      value={editRate}
                      onChange={e => setEditRate(e.target.value)}
                      className="px-3 py-1.5 rounded-lg outline-none w-20"
                      style={{ background: 'var(--muted)', border: '1px solid var(--border)', fontSize: 14 }}
                    />
                    <span style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>%</span>
                    <button onClick={() => handleUpdateRate(brand.id)}
                      className="px-3 py-1.5 rounded-lg"
                      style={{ background: 'var(--primary)', color: '#fff', fontSize: 13, fontWeight: 600 }}>
                      저장
                    </button>
                    <button onClick={() => setEditingId(null)}
                      className="px-3 py-1.5 rounded-lg"
                      style={{ background: 'var(--muted)', color: 'var(--muted-foreground)', fontSize: 13 }}>
                      취소
                    </button>
                  </>
                ) : (
                  <>
                    <span style={{ fontSize: 14, color: 'var(--foreground)' }}>수수료 {brand.commissionRate}%</span>
                    <button onClick={() => { setEditingId(brand.id); setEditRate(String(brand.commissionRate)); }}
                      className="px-3 py-1.5 rounded-lg"
                      style={{ background: 'var(--muted)', color: 'var(--muted-foreground)', fontSize: 13 }}>
                      수정
                    </button>
                  </>
                )}
              </div>

              {/* 승인/거절/삭제 버튼 */}
              <div className="flex gap-2">
                {brand.status === 'PENDING' && (
                  <>
                    <button onClick={() => handleApprove(brand.id)}
                      className="px-4 py-1.5 rounded-lg"
                      style={{ background: '#16A34A', color: '#fff', fontSize: 13, fontWeight: 600 }}>
                      승인
                    </button>
                    <button onClick={() => handleReject(brand.id)}
                      className="px-4 py-1.5 rounded-lg"
                      style={{ background: '#DC2626', color: '#fff', fontSize: 13, fontWeight: 600 }}>
                      거절
                    </button>
                  </>
                )}
                {(brand.status === 'ACTIVE' || brand.status === 'INACTIVE') && (
                  <button onClick={() => handleDelete(brand.id)}
                    className="px-4 py-1.5 rounded-lg"
                    style={{ background: '#DC2626', color: '#fff', fontSize: 13, fontWeight: 600 }}>
                    삭제
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
