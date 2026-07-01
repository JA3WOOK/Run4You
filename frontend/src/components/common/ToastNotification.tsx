import { X, Bell, Zap, CheckCircle, AlertTriangle } from "lucide-react";
import type { NotificationType } from "../../api/notification";

export interface ToastView {
    id: number;
    type: NotificationType;
    title: string;
    message: string;
}

// 백엔드 알림 유형(NotificationType) → 토스트 시각 스타일
function visualOf(type: NotificationType): "emergency" | "info" | "success" | "warning" {
    switch (type) {
        case "COMPLETED":
            return "success";
        case "ASSIGNED":
        case "DISPATCHED":
        case "ARRIVED":
        case "REPAIRING":
            return "info";
        default:
            return "info";
    }
}

// ⚠ 값·사이즈는 Figma 디자인 원본(ToastNotification) 그대로
const typeConfig = {
    emergency: { bg: "#FEF2F2", border: "#FCA5A5", icon: <Zap size={14} color="#DC2626" />, titleColor: "#DC2626" },
    info: { bg: "#EFF6FF", border: "#93C5FD", icon: <Bell size={14} color="#2563EB" />, titleColor: "#2563EB" },
    success: { bg: "#F0FDF4", border: "#86EFAC", icon: <CheckCircle size={14} color="#16A34A" />, titleColor: "#16A34A" },
    warning: { bg: "#FFFBEB", border: "#FCD34D", icon: <AlertTriangle size={14} color="#D97706" />, titleColor: "#D97706" },
};

export function ToastNotification({
    toasts,
    onDismiss,
}: {
    toasts: ToastView[];
    onDismiss: (id: number) => void;
}) {
    return (
        <div className="fixed top-4 right-4 z-50 flex flex-col gap-2" style={{ maxWidth: 320 }}>
            {toasts.map((t) => {
                const c = typeConfig[visualOf(t.type)];
                return (
                    <div
                        key={t.id}
                        className="rounded-xl p-3.5 flex gap-3 items-start shadow-lg animate-in slide-in-from-right"
                        style={{ background: c.bg, border: `1px solid ${c.border}`, minWidth: 280, backdropFilter: "blur(8px)" }}
                    >
                        <div className="mt-0.5 shrink-0">{c.icon}</div>
                        <div className="flex-1 min-w-0">
                            <div style={{ fontSize: 12, fontWeight: 700, color: c.titleColor }}>{t.title}</div>
                            <div style={{ fontSize: 11, color: "#475569", marginTop: 2 }}>{t.message}</div>
                            <div style={{ fontSize: 10, color: "#94A3B8", marginTop: 4 }}>방금 전 · SSE 실시간</div>
                        </div>
                        <button onClick={() => onDismiss(t.id)} className="shrink-0 mt-0.5">
                            <X size={13} style={{ color: "#94A3B8" }} />
                        </button>
                    </div>
                );
            })}
        </div>
    );
}
