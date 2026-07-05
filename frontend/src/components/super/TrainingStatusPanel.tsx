import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getTrainingStatus, type TrainingStatus } from '../../api/trainingStatus';

const gradeLabel: Record<string, string> = {
  BEGINNER: '초급',
  INTERMEDIATE: '중급',
  ADVANCED: '고급',
};

const th: React.CSSProperties = { textAlign: 'left', padding: '10px 12px', fontWeight: 600 };
const td: React.CSSProperties = { padding: '10px 12px' };

export default function TrainingStatusPanel() {
  const { accessToken } = useAuth();
  const [data, setData] = useState<TrainingStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!accessToken) return;
    getTrainingStatus(accessToken)
      .then(setData)
      .catch(() => setError('이수 현황을 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [accessToken]);

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;
  if (error) return <p style={{ fontSize: 13, color: 'var(--destructive)' }}>{error}</p>;
  if (!data) return null;

  const s = data.summary;
  const cards = [
    { label: '엔지니어', value: `${s.totalEngineers}명`, sub: '' },
    { label: '전체 이수율', value: `${s.completionRate}%`, sub: `${s.totalCompleted}/${s.totalEnrolled} 코스` },
    { label: '전체 합격률', value: `${s.passRate}%`, sub: `${s.totalPassed}/${s.totalAttempted} 시험` },
  ];

  return (
    <div className="flex flex-col gap-5">
      {/* 요약 카드 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
        {cards.map(c => (
          <div key={c.label} className="rounded-xl p-4" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
            <p style={{ fontSize: 12, color: 'var(--muted-foreground)', marginBottom: 6 }}>{c.label}</p>
            <p style={{ fontSize: 22, fontWeight: 700, color: 'var(--foreground)' }}>{c.value}</p>
            {c.sub && <p style={{ fontSize: 11, color: 'var(--muted-foreground)', marginTop: 2 }}>{c.sub}</p>}
          </div>
        ))}
      </div>

      {/* 엔지니어별 테이블 */}
      <div className="rounded-xl" style={{ border: '1px solid var(--border)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            <tr style={{ background: 'var(--muted)', color: 'var(--muted-foreground)' }}>
              <th style={th}>엔지니어</th>
              <th style={th}>등급</th>
              <th style={th}>이수율</th>
              <th style={th}>이수/수강</th>
              <th style={th}>합격/응시</th>
              <th style={th}>평균 진도</th>
              <th style={th}>최근 응시</th>
            </tr>
          </thead>
          <tbody>
            {data.engineers.map(e => (
              <tr key={e.engineerId} style={{ borderTop: '1px solid var(--border)', color: 'var(--foreground)' }}>
                <td style={{ ...td, fontWeight: 500 }}>{e.engineerName}</td>
                <td style={td}>{gradeLabel[e.skillGrade] ?? e.skillGrade ?? '—'}</td>
                <td style={{ ...td, fontWeight: 600 }}>{e.completionRate}%</td>
                <td style={td}>{e.completedCount}/{e.enrolledCount}</td>
                <td style={td}>{e.passedCount}/{e.attemptedCount}</td>
                <td style={td}>{e.avgProgressRate}%</td>
                <td style={{ ...td, color: 'var(--muted-foreground)' }}>
                  {e.lastAttemptedAt ? e.lastAttemptedAt.slice(0, 10) : '—'}
                </td>
              </tr>
            ))}
            {data.engineers.length === 0 && (
              <tr>
                <td colSpan={7} style={{ padding: 20, textAlign: 'center', color: 'var(--muted-foreground)' }}>
                  데이터가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
