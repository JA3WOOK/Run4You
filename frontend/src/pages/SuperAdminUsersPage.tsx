import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUsers, approveUser, rejectUser, deactivateUser, activateUser, type User } from '../api/admin';

const roleLabel: Record<string, string> = {
  SUPER_ADMIN: '플랫폼 총괄',
  BRAND_ADMIN: '본사 관리자',
  STORE_OWNER: '매장주',
  ENGINEER: '엔지니어',
};

const statusLabel: Record<string, string> = {
  PENDING: '승인 대기',
  ACTIVE: '활성',
  INACTIVE: '비활성',
};

const statusColor: Record<string, string> = {
  PENDING: '#D97706',
  ACTIVE: '#16A34A',
  INACTIVE: '#DC2626',
};

export default function SuperAdminUsersPage() {
  const { accessToken } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!accessToken) return;
    getUsers(accessToken).then(setUsers).finally(() => setLoading(false));
  }, [accessToken]);

  async function handleApprove(id: number) {
    if (!accessToken) return;
    const updated = await approveUser(accessToken, id);
    setUsers(prev => prev.map(u => u.id === id ? updated : u));
  }

  async function handleReject(id: number) {
    if (!accessToken) return;
    const updated = await rejectUser(accessToken, id);
    setUsers(prev => prev.map(u => u.id === id ? updated : u));
  }

  async function handleDeactivate(id: number) {
    if (!accessToken) return;
    const updated = await deactivateUser(accessToken, id);
    setUsers(prev => prev.map(u => u.id === id ? updated : u));
  }

  async function handleActivate(id: number) {
    if (!accessToken) return;
    const updated = await activateUser(accessToken, id);
    setUsers(prev => prev.map(u => u.id === id ? updated : u));
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;

  return (
    <div>
      <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', marginBottom: 20 }}>회원 관리</h2>

      {users.length === 0 ? (
        <div style={{ color: 'var(--muted-foreground)', fontSize: 14 }}>등록된 회원이 없습니다.</div>
      ) : (
        <div className="flex flex-col gap-3">
          {users.map(user => (
            <div key={user.id} className="rounded-xl p-5 flex items-center gap-6"
              style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>

              {/* 유저 정보 */}
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span style={{ fontSize: 16, fontWeight: 600, color: 'var(--foreground)' }}>{user.name}</span>
                  <span className="px-2 py-0.5 rounded-full" style={{
                    fontSize: 12, fontWeight: 600,
                    background: `${statusColor[user.status]}18`,
                    color: statusColor[user.status],
                  }}>
                    {statusLabel[user.status]}
                  </span>
                  <span className="px-2 py-0.5 rounded-full"
                    style={{ fontSize: 12, background: 'var(--muted)', color: 'var(--muted-foreground)' }}>
                    {roleLabel[user.role] ?? user.role}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>{user.email}</div>
                {user.brandId && (
                  <div style={{ fontSize: 12, color: 'var(--muted-foreground)', marginTop: 2 }}>브랜드 ID: {user.brandId}</div>
                )}
              </div>

              <div className="flex gap-2">
                {user.status === 'PENDING' && (
                  <>
                    <button onClick={() => handleApprove(user.id)}
                      className="px-4 py-1.5 rounded-lg"
                      style={{ background: '#16A34A', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                      승인
                    </button>
                    <button onClick={() => handleReject(user.id)}
                      className="px-4 py-1.5 rounded-lg"
                      style={{ background: '#DC2626', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                      거절
                    </button>
                  </>
                )}
                {user.status === 'ACTIVE' && user.role !== 'SUPER_ADMIN' && (
                  <button onClick={() => handleDeactivate(user.id)}
                    className="px-4 py-1.5 rounded-lg"
                    style={{ background: '#475569', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                    비활성화
                  </button>
                )}
                {user.status === 'INACTIVE' && (
                  <button onClick={() => handleActivate(user.id)}
                    className="px-4 py-1.5 rounded-lg"
                    style={{ background: '#2563EB', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                    활성화
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
