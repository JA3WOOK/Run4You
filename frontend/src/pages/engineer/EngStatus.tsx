import { useState, useRef, useEffect, type ReactNode } from "react";
import { MapPin, Wrench, CheckCircle, Navigation, Clock } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { changeStatus, pingLocation, type DispatchStatus } from "../../api/dispatch";

type Stage = 0 | 1 | 2 | 3;

// stage 0..3 → 전이시킬 상태 (ACCEPTED 에서 시작해 순서대로 전이)
const STAGES: { key: Stage; status: DispatchStatus; label: string; icon: ReactNode; color: string; bg: string; desc: string }[] = [
    { key: 0, status: "DISPATCHED", label: "출동 시작", icon: <Navigation size={18} />, color: "#2563EB", bg: "#EFF6FF", desc: "현재 이동 중 · 매장 방면" },
    { key: 1, status: "ARRIVED", label: "현장 도착", icon: <MapPin size={18} />, color: "#7C3AED", bg: "#F5F3FF", desc: "매장 도착 확인 필요" },
    { key: 2, status: "REPAIRING", label: "수리 개시", icon: <Wrench size={18} />, color: "#D97706", bg: "#FFFBEB", desc: "부품 교체 작업 중" },
    { key: 3, status: "COMPLETED", label: "수리 완료", icon: <CheckCircle size={18} />, color: "#16A34A", bg: "#F0FDF4", desc: "점주 서명 후 완료 처리" },
];

// 현재 좌표 1회 획득(출동/도착 시 ETA 산출용). 실패하면 좌표 없이 진행.
function getCoords(): Promise<{ latitude: number; longitude: number } | null> {
    return new Promise((resolve) => {
        if (!navigator.geolocation) return resolve(null);
        navigator.geolocation.getCurrentPosition(
            (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
            () => resolve(null),
            { timeout: 3000 },
        );
    });
}

// assignmentId: 수락(accept) 응답으로 받은 배정 ID. 상위(배정 상세)에서 내려준다.
export function EngStatus({
                              assignmentId,
                              onComplete,
                          }: {
    assignmentId: number;
    onComplete: () => void;
}) {
    const { accessToken } = useAuth();
    const [stage, setStage] = useState<Stage>(0);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");
    const [times, setTimes] = useState<Record<number, string>>({}); // stage → changedAt(HH:mm)

    const record = (k: number, iso: string) => {
        const d = new Date(iso);
        setTimes((t) => ({ ...t, [k]: `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}` }));
    };

    // DISPATCHED(출동 중) 단계에서만 위치 추적 — 매장 도착 전까지 10초 스로틀로 좌표 전송.
    const lastPingRef = useRef(0);
    useEffect(() => {
        if (!accessToken || stage !== 0 || !navigator.geolocation) return;
        const watchId = navigator.geolocation.watchPosition(
            (pos) => {
                const now = Date.now();
                if (now - lastPingRef.current < 10000) return; // 10초 스로틀(서버 과부하 방지)
                lastPingRef.current = now;
                pingLocation(accessToken, assignmentId, {
                    latitude: pos.coords.latitude,
                    longitude: pos.coords.longitude,
                }).catch(() => {}); // 위치 갱신 실패는 조용히 무시
            },
            () => {}, // 권한 거부 등은 무시
            { enableHighAccuracy: true, maximumAge: 5000, timeout: 8000 },
        );
        return () => navigator.geolocation.clearWatch(watchId);
    }, [accessToken, assignmentId, stage]);


    const advance = async () => {
        if (busy) return;
        setError("");

        // 마지막 단계 도달 시 리포트 작성으로 이동
        if (stage >= 3) {
            onComplete();
            return;
        }

        const next = (stage + 1) as Stage;
        const target = STAGES[next];
        setBusy(true);
        try {
            const coords = target.status === "ARRIVED" ? await getCoords() : null;
            const p = await changeStatus(accessToken ?? "", assignmentId, {
                status: target.status,
                ...(coords ?? {}),
            });
            record(next, p.changedAt);
            setStage(next);
        } catch (e: unknown) {
            const msg =
                (e as { response?: { data?: { message?: string } } })?.response?.data?.message ??
                "상태 변경에 실패했습니다. 잠시 후 다시 시도해주세요.";
            setError(msg);
            console.error("상태 변경 실패:", e);
        } finally {
            setBusy(false);
        }
    };

    const s = STAGES[stage];

    return (
        <div className="flex flex-col gap-6">
            <div>
                <h1 style={{ color: "#0F172A", letterSpacing: "-0.02em" }}>수리 상태 변경</h1>
                <p style={{ color: "#64748B", fontSize: 13, marginTop: 2 }}>
                    AS-{assignmentId} · 긴급 수리
                </p>
            </div>

            {/* Current stage highlight */}
            <div className="rounded-xl p-6 flex items-center gap-6" style={{ background: s.bg, border: `1px solid ${s.color}40` }}>
                <div className="w-16 h-16 rounded-2xl flex items-center justify-center" style={{ background: s.color }}>
                    <span style={{ color: "#fff" }}>{s.icon}</span>
                </div>
                <div>
                    <div style={{ fontSize: 11, color: s.color, fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em" }}>현재 단계</div>
                    <div style={{ fontSize: 22, fontWeight: 800, color: "#0F172A", marginTop: 4, letterSpacing: "-0.01em" }}>{s.label}</div>
                    <div style={{ fontSize: 13, color: "#64748B", marginTop: 4 }}>{s.desc}</div>
                </div>
                <div className="ml-auto flex items-center gap-2">
                    <Clock size={14} style={{ color: "#94A3B8" }} />
                    <span style={{ fontSize: 13, color: "#64748B" }}>{times[stage] ? `${times[stage]}부터` : "진행 중"}</span>
                </div>
            </div>

            {/* Stage buttons */}
            <div className="grid grid-cols-4 gap-3">
                {STAGES.map((st) => {
                    const done = st.key < stage;
                    const active = st.key === stage;
                    return (
                        <div
                            key={st.key}
                            className="rounded-xl p-4 flex flex-col items-center gap-3 transition-all"
                            style={{
                                background: active ? st.color : done ? "#F8FAFC" : "#fff",
                                border: active ? `2px solid ${st.color}` : done ? "1px solid rgba(15,23,42,0.06)" : "1px solid rgba(15,23,42,0.08)",
                                boxShadow: active ? `0 4px 12px ${st.color}30` : "0 1px 3px rgba(0,0,0,0.04)",
                                opacity: st.key > stage ? 0.5 : 1,
                            }}
                        >
                            <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: active ? "rgba(255,255,255,0.2)" : done ? "#F1F5F9" : st.bg }}>
                                <span style={{ color: active ? "#fff" : done ? "#94A3B8" : st.color }}>{st.icon}</span>
                            </div>
                            <span style={{ fontSize: 12, fontWeight: 700, color: active ? "#fff" : done ? "#94A3B8" : "#374151", textAlign: "center" }}>
                                {st.label}
                            </span>
                            {done && (
                                <div className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: "#16A34A" }}>
                                    <CheckCircle size={12} color="#fff" />
                                </div>
                            )}
                            {active && (
                                <div className="px-2 py-0.5 rounded-full" style={{ background: "rgba(255,255,255,0.25)" }}>
                                    <span style={{ fontSize: 9, color: "#fff", fontWeight: 600 }}>진행 중</span>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {/* Timeline log */}
            <div className="rounded-xl p-5" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.08)", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
                <h3 style={{ color: "#0F172A", marginBottom: 16 }}>진행 로그</h3>
                <div className="flex flex-col gap-3">
                    {STAGES.slice(0, stage + 1).map((st) => (
                        <div key={st.key} className="flex items-center gap-4">
                            <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ background: st.bg }}>
                                <span style={{ color: st.color }}>{st.icon}</span>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: "#0F172A" }}>{st.label}</div>
                                <div style={{ fontSize: 11, color: "#94A3B8" }}>
                                    {times[st.key] ?? "--:--"} · {st.desc}
                                </div>
                            </div>
                            {st.key < stage && (
                                <div className="ml-auto">
                                    <CheckCircle size={14} style={{ color: "#16A34A" }} />
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            {error && (
                <div className="px-4 py-3 rounded-xl" style={{ background: "#FEF2F2", border: "1px solid #FCA5A5", color: "#DC2626", fontSize: 13 }}>
                    {error}
                </div>
            )}

            {/* Action button */}
            <button
                onClick={advance}
                disabled={busy}
                className="py-4 rounded-xl transition-all hover:opacity-90 flex items-center justify-center gap-2"
                style={{
                    background: stage < 3 ? STAGES[stage + 1].color : "#16A34A",
                    color: "#fff",
                    fontSize: 15,
                    fontWeight: 700,
                    boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
                    opacity: busy ? 0.7 : 1,
                    cursor: busy ? "not-allowed" : "pointer",
                }}
            >
                {busy ? (
                    "처리 중..."
                ) : stage < 3 ? (
                    <>
                        {STAGES[stage + 1].icon}
                        다음 단계: {STAGES[stage + 1].label}
                    </>
                ) : (
                    <>
                        <CheckCircle size={18} />
                        수리 완료 처리
                    </>
                )}
            </button>
        </div>
    );
}