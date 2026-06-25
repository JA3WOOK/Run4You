import { MapPin, Clock, Zap, Star, ChevronRight, Navigation, Coffee, Monitor, Snowflake, Refrigerator, RefreshCw } from "lucide-react";
import { useMatchingQueue } from "../../hooks/useMatchingQueue";
import type { MatchingQueueItem } from "../../api/matching";
const catIcons: Record<string, React.ReactNode> = {
  KIOSK: <Monitor size={14} />,
  ESPRESSO: <Coffee size={14} />,
  ICE_MAKER: <Snowflake size={14} />,
  REFRIGERATOR: <Refrigerator size={14} />,
};

interface Props {
  onSelect: (asRequestId: number) => void;
}

export function EngQueue({ onSelect }: Props) {
  const { queue, loading, error, refresh } = useMatchingQueue();

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div style={{ color: "#64748B", fontSize: 14 }}>대기열 불러오는 중...</div>
    </div>
  );

  if (error) return (
    <div className="flex flex-col items-center justify-center h-64 gap-3">
      <div style={{ color: "#DC2626", fontSize: 14 }}>{error}</div>
      <button onClick={refresh} className="px-4 py-2 rounded-lg text-sm" style={{ background: "#F1F5F9", color: "#0F172A" }}>
        다시 시도
      </button>
    </div>
  );

  return (
    <div className="flex flex-col gap-6">
      {/* 헤더 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>출동 요청 대기열</h1>
          <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>반경 내 접수 요청 · 점수순 정렬</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={refresh} className="p-2 rounded-lg" style={{ background: "#F1F5F9" }}>
            <RefreshCw size={14} style={{ color: "#64748B" }} />
          </button>
          <div className="flex items-center gap-2 px-3 py-2 rounded-lg" style={{ background: "#F0FDF4", border: "1px solid #BBF7D0" }}>
            <div className="w-2 h-2 rounded-full" style={{ background: "#16A34A" }} />
            <span style={{ fontSize: 12, color: "#16A34A", fontWeight: 600 }}>출동 가능 상태</span>
          </div>
        </div>
      </div>

      {queue.length === 0 && (
        <div className="flex items-center justify-center h-48" style={{ color: "#94A3B8", fontSize: 14 }}>
          현재 접수된 요청이 없습니다.
        </div>
      )}

      {/* 대기열 카드 목록 */}
      <div className="flex flex-col gap-3">
        {queue.map((r) => (
          <QueueCard key={r.asRequestId} item={r} onSelect={onSelect} />
        ))}
      </div>
    </div>
  );
}

function QueueCard({ item: r, onSelect }: { item: MatchingQueueItem; onSelect: (id: number) => void }) {
  const isEmergency = r.priority === "EMERGENCY";

  const bars = [
    { label: "거리",    pct: r.distanceWeight,    val: r.distanceScore },
    { label: "전문분야", pct: r.specialtyWeight,   val: r.specialtyScore },
    { label: "평점",    pct: r.ratingWeight,      val: r.ratingScore },
    { label: "가용성",  pct: r.availabilityWeight, val: r.availabilityScore },
    { label: "긴급도",  pct: r.urgencyWeight,     val: r.urgencyScore },
  ];

  return (
    <div
      className="rounded-xl p-5 flex flex-col gap-4 cursor-pointer transition-all hover:shadow-md"
      style={{
        background: "#fff",
        border: isEmergency ? "1px solid #FCA5A5" : "1px solid rgba(15,23,42,0.08)",
        boxShadow: isEmergency ? "0 2px 8px rgba(220,38,38,0.08)" : "0 1px 3px rgba(0,0,0,0.04)",
      }}
      onClick={() => onSelect(r.asRequestId)}
    >
      <div className="flex items-start gap-4">
        {/* 순위 */}
        <div
          className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
          style={{ background: r.rank === 1 ? "#FEF2F2" : "#F1F5F9", border: r.rank === 1 ? "1px solid #FCA5A5" : "none" }}
        >
          <span style={{ fontSize: 14, fontWeight: 800, color: r.rank === 1 ? "#DC2626" : "#64748B" }}>#{r.rank}</span>
        </div>

        <div className="flex-1">
          <div className="flex items-center gap-2 flex-wrap">
            <span style={{ fontSize: 14, fontWeight: 700, color: "#0F172A" }}>{r.storeName}</span>
            <span
              className="px-2 py-0.5 rounded-full text-xs font-bold"
              style={{ background: isEmergency ? "#FEF2F2" : "#EFF6FF", color: isEmergency ? "#DC2626" : "#2563EB" }}
            >
              {isEmergency ? "긴급" : "일반"}
            </span>
            {r.errorCode && (
              <span className="px-2 py-0.5 rounded" style={{ background: "#FEF2F2", color: "#DC2626", fontSize: 10, fontWeight: 700, fontFamily: "monospace" }}>
                {r.errorCode}
              </span>
            )}
          </div>
          <div className="flex items-center gap-1.5 mt-1">
            <span style={{ color: "#94A3B8" }}>{catIcons[r.equipmentType] ?? null}</span>
            <span style={{ fontSize: 12, color: "#64748B" }}>{r.equipmentModel}</span>
          </div>
          <div className="flex items-center gap-4 mt-1.5">
            <span className="flex items-center gap-1" style={{ fontSize: 11, color: "#94A3B8" }}>
              <MapPin size={11} />{r.storeDistrict}
            </span>
            <span className="flex items-center gap-1" style={{ fontSize: 11, color: "#94A3B8" }}>
              <Navigation size={11} />{r.distanceKm}km
            </span>
            <span className="flex items-center gap-1" style={{ fontSize: 11, color: "#94A3B8" }}>
              <Clock size={11} />접수 {r.receivedTime}
            </span>
          </div>
        </div>

        {/* 점수 + 버튼 */}
        <div className="flex flex-col items-end gap-2 shrink-0">
          <div className="flex items-center gap-1.5">
            <Star size={12} style={{ color: "#F59E0B" }} />
            <span style={{ fontSize: 16, fontWeight: 800, color: "#0F172A" }}>{Math.round(r.totalScore)}</span>
            <span style={{ fontSize: 11, color: "#94A3B8" }}>점</span>
          </div>
          <div style={{ fontSize: 11, color: "#64748B" }}>ETA {r.etaMinutes}분</div>
          <button
            onClick={(e) => { e.stopPropagation(); onSelect(r.asRequestId); }}
            className="flex items-center gap-1.5 px-4 py-2 rounded-lg transition-all hover:opacity-90"
            style={{
              background: isEmergency ? "#DC2626" : "#2563EB",
              color: "#fff", fontSize: 12, fontWeight: 700,
              boxShadow: `0 2px 6px ${isEmergency ? "rgba(220,38,38,0.3)" : "rgba(37,99,235,0.3)"}`,
            }}
          >
            {isEmergency && <Zap size={12} />}
            수락
            <ChevronRight size={12} />
          </button>
        </div>
      </div>

      {/* 점수 바 */}
      <div className="grid grid-cols-5 gap-2 pt-3" style={{ borderTop: "1px solid rgba(15,23,42,0.05)" }}>
        {bars.map((b) => (
          <div key={b.label} className="flex flex-col gap-1">
            <div className="flex justify-between">
              <span style={{ fontSize: 10, color: "#94A3B8" }}>{b.label}</span>
              <span style={{ fontSize: 10, color: "#64748B", fontWeight: 600 }}>{b.pct}%</span>
            </div>
            <div className="h-1.5 rounded-full" style={{ background: "#F1F5F9" }}>
              <div className="h-1.5 rounded-full" style={{ width: `${b.val}%`, background: "#2563EB", opacity: 0.8 }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
