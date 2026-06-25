import { MapPin, Clock, Zap, Coffee, AlertCircle, Navigation, ArrowLeft } from "lucide-react";
import { useAssignmentDetail } from "../../hooks/useAssignmentDetail";

const SCORE_COLORS = ["#2563EB", "#16A34A", "#7C3AED", "#D97706", "#DC2626"];
const SCORE_LABELS = ["거리 가중치", "전문분야 매칭", "엔지니어 평점", "가용성", "긴급도 점수"];

interface Props {
  asRequestId: number;
  onBack: () => void;
  onAccepted: (assignmentId: number) => void;
}

export function EngDetail({ asRequestId, onBack, onAccepted }: Props) {
  const { detail: d, loading, error, accept, accepting } = useAssignmentDetail(asRequestId);

  const handleAccept = async () => {
    try {
      const assignmentId = await accept();
      onAccepted(assignmentId);
    } catch (e: any) {
      alert(e.message);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div style={{ color: "#64748B", fontSize: 14 }}>불러오는 중...</div>
    </div>
  );

  if (error || !d) return (
    <div className="flex flex-col items-center justify-center h-64 gap-3">
      <div style={{ color: "#DC2626", fontSize: 14 }}>{error ?? "데이터 없음"}</div>
      <button onClick={onBack} className="px-4 py-2 rounded-lg text-sm" style={{ background: "#F1F5F9" }}>돌아가기</button>
    </div>
  );

  const isEmergency = d.priority === "EMERGENCY";

  const weightBars = [
    { label: SCORE_LABELS[0], pct: d.distanceWeight,    score: d.distanceScore,    color: SCORE_COLORS[0] },
    { label: SCORE_LABELS[1], pct: d.specialtyWeight,   score: d.specialtyScore,   color: SCORE_COLORS[1] },
    { label: SCORE_LABELS[2], pct: d.ratingWeight,      score: d.ratingScore,      color: SCORE_COLORS[2] },
    { label: SCORE_LABELS[3], pct: d.availabilityWeight, score: d.availabilityScore, color: SCORE_COLORS[3] },
    { label: SCORE_LABELS[4], pct: d.urgencyWeight,     score: d.urgencyScore,     color: SCORE_COLORS[4] },
  ];

  const infoFields = [
    { label: "기자재명",   value: d.equipmentName },
    { label: "시리얼 번호", value: d.serialNumber, mono: true },
    { label: "구매일",     value: d.purchasedDate },
    { label: "최근 정비",  value: d.lastRepairedDate },
    { label: "에러 코드",  value: d.errorCode ?? "-", mono: true },
    { label: "매장 주소",  value: d.storeAddress },
  ];

  const trafficColor = d.trafficCondition === "원활" ? "#16A34A" : d.trafficCondition === "보통" ? "#2563EB" : "#D97706";

  return (
    <div className="flex flex-col gap-6">
      {/* 헤더 */}
      <div>
        <button onClick={onBack} className="flex items-center gap-1.5 mb-3" style={{ color: "#64748B", fontSize: 13 }}>
          <ArrowLeft size={14} /> 대기열로 돌아가기
        </button>
        <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>출동 상세 정보</h1>
        <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>{d.asRequestNo} · {d.storeName}</p>
      </div>

      <div className="grid grid-cols-3 gap-5">
        {/* 좌측: 기자재 정보 + 증상 */}
        <div className="col-span-2 flex flex-col gap-5">
          <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
            <div className="flex items-center gap-2 mb-4">
              <Coffee size={15} style={{ color: "#64748B" }} />
              <h3 style={{ color: "#0F172A" }}>고장 기자재 정보</h3>
              <span className="ml-auto px-2 py-0.5 rounded-full" style={{
                background: isEmergency ? "#FEF2F2" : "#EFF6FF",
                color: isEmergency ? "#DC2626" : "#2563EB",
                fontSize: 11, fontWeight: 700,
              }}>
                {isEmergency ? "EMERGENCY" : "NORMAL"}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-3 mb-4">
              {infoFields.map((f) => (
                <div key={f.label} className="p-3 rounded-lg" style={{ background: "#F8FAFC" }}>
                  <div style={{ fontSize: 11, color: "#94A3B8" }}>{f.label}</div>
                  <div style={{ fontSize: 12, fontWeight: 600, color: "#0F172A", marginTop: 2, fontFamily: f.mono ? "monospace" : "inherit" }}>
                    {f.value}
                  </div>
                </div>
              ))}
            </div>

            {d.symptom && (
              <div className="p-4 rounded-xl" style={{ background: "#FEF2F2", border: "1px solid #FCA5A5" }}>
                <div className="flex items-start gap-2">
                  <AlertCircle size={14} style={{ color: "#DC2626", marginTop: 1 }} />
                  <div>
                    <div style={{ fontSize: 12, fontWeight: 600, color: "#991B1B" }}>고장 증상</div>
                    <div style={{ fontSize: 12, color: "#B91C1C", marginTop: 3, lineHeight: 1.6 }}>{d.symptom}</div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 우측: 점수 + 출동 정보 */}
        <div className="flex flex-col gap-5">
          {/* 점수 카드 */}
          <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
            <h3 style={{ color: "#0F172A", marginBottom: 4 }}>가중치 배정 점수</h3>
            <p style={{ fontSize: 11, color: "#94A3B8", marginBottom: 16 }}>AI 매칭 알고리즘</p>

            {/* 도넛 총점 */}
            <div className="flex flex-col items-center py-4">
              <div className="relative w-24 h-24">
                <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
                  <circle cx={50} cy={50} r={40} fill="none" stroke="#F1F5F9" strokeWidth={12} />
                  <circle cx={50} cy={50} r={40} fill="none" stroke="#2563EB" strokeWidth={12}
                    strokeDasharray={`${(d.totalScore / 100) * 251.2} 251.2`} strokeLinecap="round" />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <div style={{ fontSize: 20, fontWeight: 800, color: "#0F172A" }}>{Math.round(d.totalScore)}</div>
                  <div style={{ fontSize: 9, color: "#94A3B8" }}>/ 100</div>
                </div>
              </div>
              <div style={{ fontSize: 11, color: "#64748B", marginTop: 8 }}>종합 배정 점수</div>
            </div>

            <div className="flex flex-col gap-3 mt-2">
              {weightBars.map((b) => (
                <div key={b.label} className="flex flex-col gap-1">
                  <div className="flex justify-between">
                    <span style={{ fontSize: 11, color: "#64748B" }}>{b.label}</span>
                    <span style={{ fontSize: 11, fontWeight: 700, color: b.color }}>{Math.round(b.score)}점</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="flex-1 h-2 rounded-full" style={{ background: "#F1F5F9" }}>
                      <div className="h-2 rounded-full" style={{ width: `${b.score}%`, background: b.color }} />
                    </div>
                    <span style={{ fontSize: 9, color: "#94A3B8", width: 28, textAlign: "right" }}>{b.pct}%</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 출동 정보 */}
          <div className="rounded-xl p-4 flex flex-col gap-3" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
            <div className="flex items-center gap-1.5">
              <Navigation size={13} style={{ color: "#64748B" }} />
              <span style={{ fontSize: 12, color: "#64748B" }}>출동 정보</span>
            </div>
            {[
              { label: "거리",      value: `${d.distanceKm}km`,    color: "#0F172A" },
              { label: "예상 시간", value: `${d.etaMinutes}분`,    color: "#0F172A" },
              { label: "교통 상황", value: d.trafficCondition,     color: trafficColor },
            ].map((row) => (
              <div key={row.label} className="flex justify-between">
                <span style={{ fontSize: 11, color: "#94A3B8" }}>{row.label}</span>
                <span style={{ fontSize: 11, fontWeight: 600, color: row.color }}>{row.value}</span>
              </div>
            ))}
          </div>

          {/* 수락 버튼 */}
          <button
            onClick={handleAccept}
            disabled={accepting}
            className="w-full py-3.5 rounded-xl flex items-center justify-center gap-2 transition-all hover:opacity-90 disabled:opacity-50"
            style={{ background: "#DC2626", color: "#fff", fontWeight: 700, fontSize: 14, boxShadow: "0 4px 12px rgba(220,38,38,0.3)" }}
          >
            <Zap size={16} />
            {accepting ? "처리 중..." : "출동 수락하기"}
          </button>
        </div>
      </div>
    </div>
  );
}
