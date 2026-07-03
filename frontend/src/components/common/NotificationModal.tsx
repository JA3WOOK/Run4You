import { useEffect, useState } from "react";
import { X, Bell, Zap, CheckCircle, AlertTriangle, CheckCheck } from "lucide-react";
import {
    getMyNotifications,
    markNotificationRead,
    markAllNotificationsRead,
    type NotificationItem,
    type NotificationType,
} from "../../api/notification";

// 백엔드 알림 유형 → 시각 스타일 (ToastNotification 과 동일 색 체계)
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

const typeConfig = {
    emergency: { bg: "#FEF2F2", border: "#FCA5A5", icon: <Zap size={14} color="#DC2626" />, color: "#DC2626" },
    info: { bg: "#EFF6FF", border: "#93C5FD", icon: <Bell size={14} color="#2563EB" />, color: "#2563EB" },
    success: { bg: "#F0FDF4", border: "#86EFAC", icon: <CheckCircle size={14} color="#16A34A" />, color: "#16A34A" },
    warning: { bg: "#FFFBEB", border: "#FCD34D", icon: <AlertTriangle size={14} color="#D97706" />, color: "#D97706" },
};

function fmtTime(iso: string) {
    const d = new Date(iso);
    const now = new Date();
    const diffMin = Math.floor((now.getTime() - d.getTime()) / 60000);
    if (diffMin < 1) return "방금 전";
    if (diffMin < 60) return `${diffMin}분 전`;
    if (diffMin < 60 * 24) return `${Math.floor(diffMin / 60)}시간 전`;
    return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

interface NotificationModalProps {
    open: boolean;
    onClose: () => void;
    accessToken: string | null;
    /** 앱 레벨 SSE 로 새 알림이 들어올 때마다 증가시키는 값 — 변하면 목록 재조회 */
    refreshSignal?: number;
    /** 읽음 처리 후 미읽음 수를 상위(배지)에 반영 */
    onUnreadChange?: (unread: number) => void;
}

export function NotificationModal({
    open,
    onClose,
    accessToken,
    refreshSignal,
    onUnreadChange,
}: NotificationModalProps) {
    const [items, setItems] = useState<NotificationItem[]>([]);
    const [loading, setLoading] = useState(false);

    // 모달 열림 + 새 알림 신호 시 목록 로드
    useEffect(() => {
        if (!open || !accessToken) return;
        setLoading(true);
        getMyNotifications(accessToken)
            .then((r) => {
                setItems(r.items);
                onUnreadChange?.(r.unreadCount);
            })
            .catch((e) => console.warn("알림 목록 로드 실패:", e))
            .finally(() => setLoading(false));
    }, [open, accessToken, refreshSignal]);

    // ESC 로 닫기
    useEffect(() => {
        if (!open) return;
        const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose();
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    if (!open) return null;

    const unread = items.filter((n) => !n.read).length;

    const handleReadOne = async (n: NotificationItem) => {
        if (n.read || !accessToken) return;
        setItems((prev) => prev.map((it) => (it.id === n.id ? { ...it, read: true } : it)));
        onUnreadChange?.(Math.max(0, unread - 1));
        try {
            await markNotificationRead(accessToken, n.id);
        } catch (e) {
            console.warn("읽음 처리 실패:", e);
        }
    };

    const handleReadAll = async () => {
        if (!accessToken || unread === 0) return;
        setItems((prev) => prev.map((it) => ({ ...it, read: true })));
        onUnreadChange?.(0);
        try {
            await markAllNotificationsRead(accessToken);
        } catch (e) {
            console.warn("전체 읽음 실패:", e);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-start justify-center"
            style={{ background: "rgba(15,23,42,0.45)", backdropFilter: "blur(2px)" }}
            onClick={onClose}
        >
            <div
                className="mt-20 w-full rounded-2xl overflow-hidden flex flex-col"
                style={{
                    maxWidth: 440,
                    maxHeight: "70vh",
                    background: "#fff",
                    border: "1px solid rgba(15,23,42,0.08)",
                    boxShadow: "0 20px 60px rgba(0,0,0,0.25)",
                }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* Header */}
                <div
                    className="flex items-center gap-2 px-5 py-4"
                    style={{ borderBottom: "1px solid rgba(15,23,42,0.06)" }}
                >
                    <Bell size={18} style={{ color: "#0F172A" }} />
                    <span style={{ fontSize: 16, fontWeight: 700, color: "#0F172A" }}>알림</span>
                    {unread > 0 && (
                        <span
                            className="px-1.5 py-0.5 rounded-full"
                            style={{ background: "#DC2626", color: "#fff", fontSize: 11, fontWeight: 700 }}
                        >
                            {unread}
                        </span>
                    )}
                    <div className="ml-auto flex items-center gap-1">
                        {unread > 0 && (
                            <button
                                onClick={handleReadAll}
                                className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg transition-colors"
                                style={{ fontSize: 12, color: "#2563EB", fontWeight: 600 }}
                            >
                                <CheckCheck size={13} /> 모두 읽음
                            </button>
                        )}
                        <button
                            onClick={onClose}
                            className="w-8 h-8 rounded-lg flex items-center justify-center transition-colors"
                            style={{ color: "#94A3B8" }}
                            aria-label="닫기"
                        >
                            <X size={18} />
                        </button>
                    </div>
                </div>

                {/* List */}
                <div className="flex-1 overflow-y-auto" style={{ padding: 8 }}>
                    {loading ? (
                        <div className="flex items-center justify-center py-16" style={{ color: "#94A3B8", fontSize: 13 }}>
                            불러오는 중...
                        </div>
                    ) : items.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-16 gap-2">
                            <Bell size={28} style={{ color: "#CBD5E1" }} />
                            <span style={{ color: "#94A3B8", fontSize: 13 }}>새로운 알림이 없습니다.</span>
                        </div>
                    ) : (
                        items.map((n) => {
                            const c = typeConfig[visualOf(n.type)];
                            return (
                                <button
                                    key={n.id}
                                    onClick={() => handleReadOne(n)}
                                    className="w-full flex gap-3 items-start px-3 py-3 rounded-xl transition-colors text-left"
                                    style={{
                                        background: n.read ? "transparent" : c.bg,
                                        border: `1px solid ${n.read ? "transparent" : c.border}`,
                                        marginBottom: 4,
                                        cursor: n.read ? "default" : "pointer",
                                    }}
                                >
                                    <div
                                        className="w-7 h-7 rounded-lg flex items-center justify-center shrink-0 mt-0.5"
                                        style={{ background: n.read ? "#F1F5F9" : "#fff" }}
                                    >
                                        {c.icon}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center gap-2">
                                            <span
                                                style={{
                                                    fontSize: 13,
                                                    fontWeight: 700,
                                                    color: n.read ? "#64748B" : c.color,
                                                }}
                                            >
                                                {n.title}
                                            </span>
                                            {!n.read && (
                                                <span
                                                    className="w-1.5 h-1.5 rounded-full shrink-0"
                                                    style={{ background: "#DC2626" }}
                                                />
                                            )}
                                            <span className="ml-auto shrink-0" style={{ fontSize: 11, color: "#94A3B8" }}>
                                                {fmtTime(n.createdAt)}
                                            </span>
                                        </div>
                                        <div style={{ fontSize: 12, color: "#64748B", marginTop: 2 }}>{n.message}</div>
                                    </div>
                                </button>
                            );
                        })
                    )}
                </div>
            </div>
        </div>
    );
}
