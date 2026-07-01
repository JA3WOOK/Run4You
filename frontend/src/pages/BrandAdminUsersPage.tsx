import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getPendingUsers, approveUser, rejectUser, type User } from '../api/admin';

const roleLabel: Record<string, string> = {
  STORE_OWNER: '점주',
  ENGINEER: '엔지니어',
};

export default function BrandAdminUsersPage() {
  const { accessToken } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!accessToken) return;
    getPendingUsers(accessToken).then(setUsers).finally(() => setLoading(false));
  }, [accessToken]);

  async function handleApprove(id: number) {
    if (!accessToken) return;
    const updated = await approveUser(accessToken, id);
    setUsers(prev => prev.map(u => u.id === id ? updated : u));
  }

  async function handleReject(id: number) {
    if (!accessToken) return;
    await rejectUser(accessToken, id);
    setUsers(prev => prev.filter(u => u.id !== id));
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;

  const pending = users.filter(u => u.status === 'PENDING');

  return (
    <div>
      <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', marginBottom: 20 }}>회원 승인 관리</h2>

      {pending.length === 0 ? (
        <div style={{ color: 'var(--muted-foreground)', fontSize: 14 }}>승인 대기 중인 회원이 없습니다.</div>
      ) : (
        <div className="flex flex-col gap-3">
          {pending.map(user => (
            <div key={user.id} className="rounded-xl p-5 flex items-center gap-6"
              style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>

              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span style={{ fontSize: 16, fontWeight: 600, color: 'var(--foreground)' }}>{user.name}</span>
                  <span className="px-2 py-0.5 rounded-full"
                    style={{ fontSize: 12, fontWeight: 600, background: '#D9770618', color: '#D97706' }}>
                    승인 대기
                  </span>
                  <span className="px-2 py-0.5 rounded-full"
                    style={{ fontSize: 12, background: 'var(--muted)', color: 'var(--muted-foreground)' }}>
                    {roleLabel[user.role] ?? user.role}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>{user.email}</div>
              </div>

              <div className="flex gap-2">
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
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}