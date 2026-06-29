import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Zap } from 'lucide-react';
import { signup, signupBrand } from '../api/auth';

type Tab = 'brand' | 'member';

export default function SignupPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>('brand');
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // 브랜드 가입 폼
  const [brandForm, setBrandForm] = useState({
    brandName: '', businessNo: '', commissionRate: '',
    adminEmail: '', adminPassword: '', adminName: '', adminPhone: '',
  });

  // 일반 가입 폼
  const [memberForm, setMemberForm] = useState({
    email: '', password: '', name: '', phone: '',
    role: 'STORE_OWNER' as 'STORE_OWNER' | 'ENGINEER',
    brandId: '',
  });
  const [specialties, setSpecialties] = useState<string[]>([]);

  const SPECIALTY_OPTIONS = [
    { value: 'KIOSK', label: '키오스크' },
    { value: 'ESPRESSO', label: '에스프레소 머신' },
    { value: 'ICE_MAKER', label: '제빙기' },
    { value: 'REFRIGERATOR', label: '냉장·냉동 장비' },
  ];

  function toggleSpecialty(value: string) {
    setSpecialties(prev =>
      prev.includes(value) ? prev.filter(s => s !== value) : [...prev, value]
    );
  }

  async function handleBrandSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(''); setSuccess(''); setLoading(true);
    try {
      await signupBrand({
        ...brandForm,
        commissionRate: Number(brandForm.commissionRate),
      });
      setSuccess('브랜드 가입 신청이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message;
      setError(message ?? '가입 신청에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }

  async function handleMemberSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(''); setSuccess(''); setLoading(true);
    try {
      await signup({
        ...memberForm,
        brandId: Number(memberForm.brandId),
        ...(memberForm.role === 'ENGINEER' && { specialties }),
      });
      setSuccess('가입 신청이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message;
      setError(message ?? '가입 신청에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }

  const inputStyle = {
    background: 'var(--muted)',
    border: '1px solid var(--border)',
    color: 'var(--foreground)',
    fontSize: 15,
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12" style={{ background: 'var(--background)' }}>
      <div className="w-full max-w-lg">
        {/* Logo */}
        <div className="flex items-center justify-center gap-3 mb-8">
          <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: 'var(--primary)' }}>
            <Zap size={20} color="#fff" />
          </div>
          <div>
            <div style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)' }}>Run4You</div>
            <div style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>긴급 A/S 관제 플랫폼</div>
          </div>
        </div>

        {/* Card */}
        <div className="rounded-2xl p-8" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
          <h1 style={{ fontSize: 22, fontWeight: 700, color: 'var(--foreground)', marginBottom: 20 }}>가입 신청</h1>

          {/* Tabs */}
          <div className="flex gap-1 p-1 rounded-lg mb-6" style={{ background: 'var(--muted)' }}>
            {(['brand', 'member'] as Tab[]).map(t => (
              <button
                key={t}
                onClick={() => { setTab(t); setError(''); setSuccess(''); }}
                className="flex-1 py-2 rounded-md transition-all"
                style={{
                  fontSize: 14,
                  fontWeight: tab === t ? 600 : 400,
                  background: tab === t ? 'var(--card)' : 'transparent',
                  color: tab === t ? 'var(--foreground)' : 'var(--muted-foreground)',
                  boxShadow: tab === t ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                }}
              >
                {t === 'brand' ? '브랜드 가입' : '매장주 / 엔지니어'}
              </button>
            ))}
          </div>

          {success && (
            <div className="p-3 rounded-lg mb-4" style={{ background: 'var(--success-bg)', color: 'var(--success)', fontSize: 13 }}>
              {success}
            </div>
          )}
          {error && (
            <p style={{ fontSize: 13, color: 'var(--destructive)', marginBottom: 12 }}>{error}</p>
          )}

          {/* 브랜드 가입 폼 */}
          {tab === 'brand' && (
            <form onSubmit={handleBrandSubmit} className="flex flex-col gap-4">
              <p style={{ fontSize: 13, color: 'var(--muted-foreground)', marginBottom: 4 }}>브랜드 정보</p>
              <div className="flex flex-col gap-1.5">
                <label style={{ fontSize: 14 }}>브랜드명</label>
                <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={brandForm.brandName} onChange={e => setBrandForm(p => ({ ...p, brandName: e.target.value }))}
                  placeholder="메가커피" required />
              </div>
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>사업자 번호</label>
                  <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={brandForm.businessNo} onChange={e => setBrandForm(p => ({ ...p, businessNo: e.target.value }))}
                    placeholder="123-45-67890" required />
                </div>
                <div className="w-32 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>수수료율 (%)</label>
                  <input type="number" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={brandForm.commissionRate} onChange={e => setBrandForm(p => ({ ...p, commissionRate: e.target.value }))}
                    placeholder="10" min="0" required />
                </div>
              </div>
              <p style={{ fontSize: 13, color: 'var(--muted-foreground)', marginTop: 4 }}>담당자 정보</p>
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>이름</label>
                  <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={brandForm.adminName} onChange={e => setBrandForm(p => ({ ...p, adminName: e.target.value }))}
                    placeholder="홍길동" required />
                </div>
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>연락처</label>
                  <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={brandForm.adminPhone} onChange={e => setBrandForm(p => ({ ...p, adminPhone: e.target.value }))}
                    placeholder="010-1234-5678" />
                </div>
              </div>
              <div className="flex flex-col gap-1.5">
                <label style={{ fontSize: 14 }}>이메일</label>
                <input type="email" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={brandForm.adminEmail} onChange={e => setBrandForm(p => ({ ...p, adminEmail: e.target.value }))}
                  placeholder="admin@brand.com" required />
              </div>
              <div className="flex flex-col gap-1.5">
                <label style={{ fontSize: 14 }}>비밀번호</label>
                <input type="password" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={brandForm.adminPassword} onChange={e => setBrandForm(p => ({ ...p, adminPassword: e.target.value }))}
                  placeholder="비밀번호를 입력하세요" required />
              </div>
              <button type="submit" disabled={loading} className="w-full py-3 rounded-lg mt-2 transition-opacity"
                style={{ background: 'var(--primary)', color: '#fff', fontSize: 15, fontWeight: 600, opacity: loading ? 0.7 : 1 }}>
                {loading ? '신청 중...' : '가입 신청'}
              </button>
            </form>
          )}

          {/* 매장주/엔지니어 가입 폼 */}
          {tab === 'member' && (
            <form onSubmit={handleMemberSubmit} className="flex flex-col gap-4">
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>이름</label>
                  <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={memberForm.name} onChange={e => setMemberForm(p => ({ ...p, name: e.target.value }))}
                    placeholder="홍길동" required />
                </div>
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>연락처</label>
                  <input className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={memberForm.phone} onChange={e => setMemberForm(p => ({ ...p, phone: e.target.value }))}
                    placeholder="010-1234-5678" />
                </div>
              </div>
              <div className="flex flex-col gap-1.5">
                <label style={{ fontSize: 14 }}>이메일</label>
                <input type="email" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={memberForm.email} onChange={e => setMemberForm(p => ({ ...p, email: e.target.value }))}
                  placeholder="email@example.com" required />
              </div>
              <div className="flex flex-col gap-1.5">
                <label style={{ fontSize: 14 }}>비밀번호</label>
                <input type="password" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                  value={memberForm.password} onChange={e => setMemberForm(p => ({ ...p, password: e.target.value }))}
                  placeholder="비밀번호를 입력하세요" required />
              </div>
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>역할</label>
                  <select className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={memberForm.role} onChange={e => { setMemberForm(p => ({ ...p, role: e.target.value as 'STORE_OWNER' | 'ENGINEER' })); setSpecialties([]); }}>
                    <option value="STORE_OWNER">매장주</option>
                    <option value="ENGINEER">엔지니어</option>
                  </select>
                </div>
                <div className="flex-1 flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>브랜드 ID</label>
                  <input type="number" className="w-full px-4 py-2.5 rounded-lg outline-none" style={inputStyle}
                    value={memberForm.brandId} onChange={e => setMemberForm(p => ({ ...p, brandId: e.target.value }))}
                    placeholder="1" required />
                </div>
              </div>
              {memberForm.role === 'ENGINEER' && (
                <div className="flex flex-col gap-1.5">
                  <label style={{ fontSize: 14 }}>전문 분야 <span style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>(복수 선택 가능)</span></label>
                  <div className="flex flex-wrap gap-2 p-3 rounded-lg" style={{ background: 'var(--muted)', border: '1px solid var(--border)' }}>
                    {SPECIALTY_OPTIONS.map(opt => (
                      <label key={opt.value} className="flex items-center gap-1.5 cursor-pointer px-3 py-1.5 rounded-lg transition-all"
                        style={{
                          background: specialties.includes(opt.value) ? 'var(--primary)' : 'var(--card)',
                          color: specialties.includes(opt.value) ? '#fff' : 'var(--foreground)',
                          fontSize: 13, fontWeight: 500,
                          border: `1px solid ${specialties.includes(opt.value) ? 'var(--primary)' : 'var(--border)'}`,
                        }}>
                        <input type="checkbox" checked={specialties.includes(opt.value)}
                          onChange={() => toggleSpecialty(opt.value)} style={{ display: 'none' }} />
                        {opt.label}
                      </label>
                    ))}
                  </div>
                </div>
              )}
              <button type="submit" disabled={loading} className="w-full py-3 rounded-lg mt-2 transition-opacity"
                style={{ background: 'var(--primary)', color: '#fff', fontSize: 15, fontWeight: 600, opacity: loading ? 0.7 : 1 }}>
                {loading ? '신청 중...' : '가입 신청'}
              </button>
            </form>
          )}

          <div className="mt-6 text-center" style={{ fontSize: 14, color: 'var(--muted-foreground)' }}>
            이미 계정이 있으신가요?{' '}
            <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600 }}>로그인</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
