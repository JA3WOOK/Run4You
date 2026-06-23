import { useState, useEffect } from "react";
import { X, Bell, Zap, CheckCircle, AlertTriangle } from "lucide-react";

interface Toast {
    id: number;
    type: "emergency" | "info" | "success" | "warning";
    title: string;
    message: string;
}

const MOCK_TOASTS: Omit<Toast, "id">[] = [
    { type: "emergency", title: "긴급 A/S 접수", message: "강남점 에스프레소 머신 고장 신고" },
    { type: "info", title: "엔지니어 배정 완료", message: "박성민 엔지니어 출동 예정 - ETA 23분" },
    { type: "success", title: "수리 완료", message: "신촌점 냉장고 수리가 완료되었습니다" },
    { type: "warning", title: "처리 지연 경고", message: "홍대점 키오스크 접수 후 2시간 경과" },
];

const typeConfig = {
    emergency: { bg: "#FEF2F2", border: "#FCA5A5", icon: <Zap size={17} color="#DC2626" />, titleColor: "#DC2626" },
    info: { bg: "#EFF6FF", border: "#93C5FD", icon: <Bell size={17} color="#2563EB" />, titleColor: "#2563EB" },
    success: { bg: "#F0FDF4", border: "#86EFAC", icon: <CheckCircle size={17} color="#16A34A" />, titleColor: "#16A34A" },
    warning: { bg: "#FFFBEB", border: "#FCD34D", icon: <AlertTriangle size={17} color="#D97706" />, titleColor: "#D97706" },
};

export function ToastNotification() {
    const [toasts, setToasts] = useState<Toast[]>([]);

    useEffect(() => {
        let idx = 0;
        const show = () => {
            const t = MOCK_TOASTS[idx % MOCK_TOASTS.length];
            const id = Date.now();
            setToasts((prev) => [...prev, { ...t, id }]);
            setTimeout(() => setToasts((prev) => prev.filter((x) => x.id !== id)), 5000);
            idx++;
        };
        const timer = setInterval(show, 7000);
        const first = setTimeout(show, 2000);
        return () => { clearInterval(timer); clearTimeout(first); };
    }, []);

    const dismiss = (id: number) => setToasts((prev) => prev.filter((x) => x.id !== id));

    return (
        <div className="fixed top-4 right-4 z-50 flex flex-col gap-2" style={{ maxWidth: 380 }}>
            {toasts.map((t) => {
                const c = typeConfig[t.type];
                return (
                    <div
                        key={t.id}
                        className="rounded-xl p-4 flex gap-3 items-start shadow-lg"
                        style={{ background: c.bg, border: `1px solid ${c.border}`, minWidth: 340, backdropFilter: "blur(8px)" }}
                    >
                        <div className="mt-0.5 shrink-0">{c.icon}</div>
                        <div className="flex-1 min-w-0">
                            <div style={{ fontSize: 14, fontWeight: 700, color: c.titleColor }}>{t.title}</div>
                            <div style={{ fontSize: 13, color: "#475569", marginTop: 3 }}>{t.message}</div>
                            <div style={{ fontSize: 11, color: "#94A3B8", marginTop: 5 }}>방금 전 · SSE 실시간</div>
                        </div>
                        <button onClick={() => dismiss(t.id)} className="shrink-0 mt-0.5">
                            <X size={15} style={{ color: "#94A3B8" }} />
                        </button>
                    </div>
                );
            })}
        </div>
    );
}