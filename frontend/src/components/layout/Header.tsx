import { useState, useEffect } from "react";

interface HeaderProps {
    screenLabel: string;
    currentTime?: string;   // 넘기면 그 값 사용, 없으면 실시간 시계
    sseConnected?: boolean; // SSE 실시간 연결 상태 (undefined면 연결됨으로 간주)
}

// 팀원 포맷(ko-KR) 채택: "2026. 07. 02. 14:30"
function fmtNow(d: Date) {
    return d.toLocaleString("ko-KR", {
        year: "numeric", month: "2-digit", day: "2-digit",
        hour: "2-digit", minute: "2-digit", hour12: false,
    });
}

export function Header({ screenLabel, currentTime, sseConnected }: HeaderProps) {
    const [now, setNow] = useState(() => new Date());
    const connected = sseConnected !== false; // undefined/true → 연결됨

    useEffect(() => {
        if (currentTime) return;              // 외부 주입값이 있으면 시계 안 돌림
        const id = setInterval(() => setNow(new Date()), 60000); // 분 단위 표시 → 60초 간격
        return () => clearInterval(id);
    }, [currentTime]);

    const display = currentTime ?? fmtNow(now);

    return (
        <div
            className="sticky top-0 z-10 flex items-center justify-between px-8 py-5"
            style={{ background: "rgba(248,250,252,0.9)", backdropFilter: "blur(8px)", borderBottom: "1px solid rgba(15,23,42,0.06)" }}
        >
            {/* 좌측: 빵부스러기 */}
            <div className="flex items-center gap-2" style={{ fontSize: 14, color: "#94A3B8" }}>
                <span style={{ color: "#64748B" }}>Run4You</span>
                <span>/</span>
                <span style={{ color: "#0F172A", fontWeight: 500 }}>{screenLabel}</span>
            </div>

            {/* 우측: SSE 상태 + 시간 */}
            <div className="flex items-center gap-3">
                <div
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-full"
                    style={{ background: connected ? "#F0FDF4" : "#FFFBEB", border: `1px solid ${connected ? "#BBF7D0" : "#FDE68A"}` }}
                >
                    <div className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: connected ? "#16A34A" : "#D97706" }} />
                    <span style={{ fontSize: 13, color: connected ? "#16A34A" : "#D97706", fontWeight: 600 }}>{connected ? "SSE 연결됨" : "SSE 재연결 중"}</span>
                </div>
                {display && (
                    <span style={{ fontSize: 14, color: "#94A3B8" }}>{display}</span>
                )}
            </div>
        </div>
    );
}