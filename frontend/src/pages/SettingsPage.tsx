import { useEffect, useState } from 'react';
import { User, Mail, Phone, BadgeCheck, Star, Save } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getMyProfile, updateMyProfile, type MyProfile } from '../api/auth';

type Tab = 'profile' | 'basic' | 'password';

const roleLabel: Record<string, string> = {
  STORE_OWNER: '점주',
  ENGINEER: '엔지니어',
  BRAND_ADMIN: '본사 관리자',
  SUPER_ADMIN: '플랫폼 총괄',
};

const specialtyLabel: Record<string, string> = {
  KIOSK: '키오스크',
  ESPRESSO: '에스프레소 머신',
  ICE_MAKER: '제빙기',
  REFRIGERATOR: '냉장·냉동 장비',
};

const inputStyle = {
  background: 'var(--muted)',
  border: '1px solid var(--border)',
  color: 'var(--foreground)',
  fontSize: 14,
};

export function SettingsPage() {
  const { accessToken } = useAuth();
  const [tab, setTab] = useState<Tab>('profile');
  const [profile, setProfile] = useState<MyProfile | null>(null);
  const [loading, setLoading] = useState(true);

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!accessToken) return;
    getMyProfile(accessToken).then(p => {
      setProfile(p);
      setName(p.name);
      setPhone(p.phone ?? '');
    }).finally(() => setLoading(false));
  }, [accessToken]);

  function handleTabChange(t: Tab) {
    setTab(t);
    setSuccessMsg('');
    setErrorMsg('');
  }

  async function handleSaveBasic(e: React.FormEvent) {
    e.preventDefault();
    if (!accessToken) return;
    setErrorMsg(''); setSuccessMsg('');
    setSaving(true);
    try {
      const updated = await updateMyProfile(accessToken, { name, phone });
      setProfile(updated);
      setSuccessMsg('저장되었습니다.');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setErrorMsg(msg ?? '저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  async function handleSavePassword(e: React.FormEvent) {
    e.preventDefault();
    if (!accessToken) return;
    setErrorMsg(''); setSuccessMsg('');
    if (newPassword !== confirmPassword) {
      setErrorMsg('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    setSaving(true);
    try {
      await updateMyProfile(accessToken, { currentPassword, newPassword });
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
      setSuccessMsg('비밀번호가 변경되었습니다.');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setErrorMsg(msg ?? '저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;
  if (!profile) return <div style={{ color: 'var(--destructive)', padding: 32 }}>데이터를 불러오지 못했습니다.</div>;

  const tabs: { key: Tab; label: string }[] = [
    { key: 'profile', label: '내 프로필' },
    { key: 'basic', label: '기본 정보' },
    { key: 'password', label: '비밀번호 변경' },
  ];

  return (
    <div className="flex flex-col gap-6" style={{ maxWidth: 600 }}>
      <div>
        <h1 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', letterSpacing: '-0.02em' }}>설정</h1>
      </div>

      {/* 탭 */}
      <div className="flex gap-1 p-1 rounded-lg" style={{ background: 'var(--muted)' }}>
        {tabs.map(t => (
          <button key={t.key} onClick={() => handleTabChange(t.key)}
            className="flex-1 py-2 rounded-md transition-all"
            style={{
              fontSize: 14,
              fontWeight: tab === t.key ? 600 : 400,
              background: tab === t.key ? 'var(--card)' : 'transparent',
              color: tab === t.key ? 'var(--foreground)' : 'var(--muted-foreground)',
              boxShadow: tab === t.key ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              cursor: 'pointer',
            }}>
            {t.label}
          </button>
        ))}
      </div>

      {/* 내 프로필 탭 */}
      {tab === 'profile' && (
        <div className="flex flex-col gap-4">
          <div className="rounded-xl p-6 flex flex-col gap-4" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-full flex items-center justify-center shrink-0"
                style={{ background: 'var(--primary)', color: '#fff', fontSize: 22, fontWeight: 700 }}>
                {profile.name[0]?.toUpperCase()}
              </div>
              <div>
                <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--foreground)' }}>{profile.name}</div>
                <span className="px-2 py-0.5 rounded-full mt-1 inline-block"
                  style={{ fontSize: 12, fontWeight: 600, background: '#7C3AED18', color: '#7C3AED' }}>
                  {roleLabel[profile.role] ?? profile.role}
                </span>
              </div>
            </div>
            <div className="flex flex-col gap-3 pt-2" style={{ borderTop: '1px solid var(--border)' }}>
              <div className="flex items-center gap-3">
                <Mail size={15} style={{ color: 'var(--muted-foreground)', flexShrink: 0 }} />
                <span style={{ fontSize: 14, color: 'var(--foreground)' }}>{profile.email}</span>
              </div>
              {profile.phone && (
                <div className="flex items-center gap-3">
                  <Phone size={15} style={{ color: 'var(--muted-foreground)', flexShrink: 0 }} />
                  <span style={{ fontSize: 14, color: 'var(--foreground)' }}>{profile.phone}</span>
                </div>
              )}
              {profile.brandName && (
                <div className="flex items-center gap-3">
                  <User size={15} style={{ color: 'var(--muted-foreground)', flexShrink: 0 }} />
                  <span style={{ fontSize: 14, color: 'var(--foreground)' }}>{profile.brandName}</span>
                </div>
              )}
            </div>
          </div>

          {profile.role === 'ENGINEER' && (
            <div className="rounded-xl p-6 flex flex-col gap-4" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
              <h2 style={{ fontSize: 15, fontWeight: 700, color: 'var(--foreground)' }}>엔지니어 정보</h2>
              <div className="flex gap-4">
                <div className="flex-1 rounded-lg p-4 flex flex-col gap-1" style={{ background: 'var(--muted)' }}>
                  <div className="flex items-center gap-1.5">
                    <Star size={14} style={{ color: '#D97706' }} />
                    <span style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>평점</span>
                  </div>
                  <span style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)' }}>
                    {profile.rating != null ? profile.rating.toFixed(2) : '—'}
                  </span>
                </div>
                <div className="flex-1 rounded-lg p-4 flex flex-col gap-1" style={{ background: 'var(--muted)' }}>
                  <div className="flex items-center gap-1.5">
                    <BadgeCheck size={14} style={{ color: '#7C3AED' }} />
                    <span style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>기술 등급</span>
                  </div>
                  <span style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)' }}>
                    {profile.skillGrade ?? '—'}
                  </span>
                </div>
              </div>
              <div className="flex flex-col gap-2">
                <span style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>전문 분야</span>
                {profile.specialties.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {profile.specialties.map(s => (
                      <span key={s} className="px-3 py-1.5 rounded-lg"
                        style={{ background: '#7C3AED18', color: '#7C3AED', fontSize: 13, fontWeight: 500 }}>
                        {specialtyLabel[s] ?? s}
                      </span>
                    ))}
                  </div>
                ) : (
                  <span style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>등록된 전문 분야가 없습니다.</span>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* 기본 정보 탭 */}
      {tab === 'basic' && (
        <form onSubmit={handleSaveBasic} className="flex flex-col gap-4">
          <div className="rounded-xl p-6 flex flex-col gap-4" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
            <div className="flex flex-col gap-1.5">
              <label style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>이름</label>
              <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                value={name} onChange={e => setName(e.target.value)} placeholder="이름" required />
            </div>
            <div className="flex flex-col gap-1.5">
              <label style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>연락처</label>
              <div className="relative">
                <Phone size={14} style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', color: 'var(--muted-foreground)' }} />
                <input className="w-full pl-9 pr-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={phone} onChange={e => setPhone(e.target.value)} placeholder="010-0000-0000" />
              </div>
            </div>
          </div>
          {successMsg && <p style={{ fontSize: 13, color: '#16A34A' }}>{successMsg}</p>}
          {errorMsg && <p style={{ fontSize: 13, color: 'var(--destructive)' }}>{errorMsg}</p>}
          <button type="submit" disabled={saving} className="flex items-center justify-center gap-2 w-full py-3 rounded-lg transition-opacity"
            style={{ background: 'var(--primary)', color: '#fff', fontSize: 15, fontWeight: 600, opacity: saving ? 0.7 : 1, cursor: 'pointer' }}>
            <Save size={16} />
            {saving ? '저장 중...' : '저장'}
          </button>
        </form>
      )}

      {/* 비밀번호 변경 탭 */}
      {tab === 'password' && (
        <form onSubmit={handleSavePassword} className="flex flex-col gap-4">
          <div className="rounded-xl p-6 flex flex-col gap-4" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
            <div className="flex flex-col gap-1.5">
              <label style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>현재 비밀번호</label>
              <input type="password" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} placeholder="현재 비밀번호" required />
            </div>
            <div className="flex flex-col gap-1.5">
              <label style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>새 비밀번호</label>
              <input type="password" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                value={newPassword} onChange={e => setNewPassword(e.target.value)} placeholder="새 비밀번호" required />
            </div>
            <div className="flex flex-col gap-1.5">
              <label style={{ fontSize: 13, color: 'var(--muted-foreground)' }}>새 비밀번호 확인</label>
              <input type="password" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} placeholder="새 비밀번호 확인" required />
            </div>
          </div>
          {successMsg && <p style={{ fontSize: 13, color: '#16A34A' }}>{successMsg}</p>}
          {errorMsg && <p style={{ fontSize: 13, color: 'var(--destructive)' }}>{errorMsg}</p>}
          <button type="submit" disabled={saving} className="flex items-center justify-center gap-2 w-full py-3 rounded-lg transition-opacity"
            style={{ background: 'var(--primary)', color: '#fff', fontSize: 15, fontWeight: 600, opacity: saving ? 0.7 : 1, cursor: 'pointer' }}>
            <Save size={16} />
            {saving ? '저장 중...' : '비밀번호 변경'}
          </button>
        </form>
      )}
    </div>
  );
}
