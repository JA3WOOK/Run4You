interface HeaderProps {
    screenLabel: string;
    currentTime?: string;
}

export function Header({ screenLabel, currentTime = "" }: HeaderProps) {
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
                <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full" style={{ background: "#F0FDF4", border: "1px solid #BBF7D0" }}>
                    <div className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: "#16A34A" }} />
                    <span style={{ fontSize: 13, color: "#16A34A", fontWeight: 600 }}>SSE 연결됨</span>
                </div>
                {currentTime && (
                    <span style={{ fontSize: 14, color: "#94A3B8" }}>{currentTime}</span>
                )}
            </div>
        </div>
    );
}