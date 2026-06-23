import {
    LayoutDashboard, Wrench, MapPin, FileText, ClipboardList,
    Settings, Bell, ChevronRight, LogOut, Zap, BarChart3,
    CreditCard, Package
} from "lucide-react";

export type UserRole = "STORE_OWNER" | "ENGINEER" | "BRAND_ADMIN" | "SUPER_ADMIN";
export type Screen =
    | "store-home" | "store-as-form" | "store-dispatch" | "store-receipt"
    | "eng-queue" | "eng-detail" | "eng-status" | "eng-report"
    | "admin-dashboard" | "admin-equipment" | "admin-billing"
    | "super-dashboard";

interface SidebarProps {
    role: UserRole;
    screen: Screen;
    onScreenChange: (s: Screen) => void;
    onRoleChange: (r: UserRole) => void;
    notifications: number;
}

const roleLabels: Record<UserRole, string> = {
    STORE_OWNER: "점주",
    ENGINEER: "엔지니어",
    BRAND_ADMIN: "본사 관리자",
    SUPER_ADMIN: "플랫폼 총괄",
};

const roleColors: Record<UserRole, string> = {
    STORE_OWNER: "#16A34A",
    ENGINEER: "#D97706",
    BRAND_ADMIN: "#2563EB",
    SUPER_ADMIN: "#7C3AED",
};

const navItems: Record<UserRole, { label: string; screen: Screen; icon: React.ReactNode }[]> = {
    STORE_OWNER: [
        { label: "기자재 현황", screen: "store-home", icon: <LayoutDashboard size={19} /> },
        { label: "긴급 A/S 접수", screen: "store-as-form", icon: <Zap size={19} /> },
        { label: "출동 현황", screen: "store-dispatch", icon: <MapPin size={19} /> },
        { label: "진단서 / 영수증", screen: "store-receipt", icon: <FileText size={19} /> },
    ],
    ENGINEER: [
        { label: "출동 요청 대기열", screen: "eng-queue", icon: <ClipboardList size={19} /> },
        { label: "출동 상세", screen: "eng-detail", icon: <MapPin size={19} /> },
        { label: "수리 상태 변경", screen: "eng-status", icon: <Wrench size={19} /> },
        { label: "정비 리포트", screen: "eng-report", icon: <FileText size={19} /> },
    ],
    BRAND_ADMIN: [
        { label: "통합 관제 대시보드", screen: "admin-dashboard", icon: <LayoutDashboard size={19} /> },
        { label: "기자재 관리", screen: "admin-equipment", icon: <Package size={19} /> },
        { label: "정산 관리", screen: "admin-billing", icon: <CreditCard size={19} /> },
    ],
    SUPER_ADMIN: [
        { label: "전체 통계 대시보드", screen: "super-dashboard", icon: <BarChart3 size={19} /> },
    ],
};

export function Sidebar({ role, screen, onScreenChange, onRoleChange, notifications }: SidebarProps) {
    const items = navItems[role];

    return (
        <aside
            className="flex flex-col h-screen w-72 shrink-0"
            style={{ background: "var(--navy)", borderRight: "1px solid rgba(255,255,255,0.06)" }}
        >
            {/* Logo */}
            <div className="px-5 py-5 flex items-center gap-2.5" style={{ borderBottom: "1px solid rgba(255,255,255,0.06)" }}>
                <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "var(--primary)" }}>
                    <Zap size={18} color="#fff" />
                </div>
                <div>
                    <div style={{ color: "#F1F5F9", fontWeight: 700, fontSize: 17, letterSpacing: "-0.01em" }}>Run4You</div>
                    <div style={{ color: "#64748B", fontSize: 12 }}>긴급 A/S 관제 플랫폼</div>
                </div>
            </div>

            {/* Role switcher */}
            <div className="px-3 py-3">
                <div style={{ fontSize: 11, color: "#475569", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6, paddingLeft: 8 }}>
                    역할 전환
                </div>
                <div className="grid grid-cols-2 gap-1">
                    {(Object.keys(roleLabels) as UserRole[]).map((r) => (
                        <button
                            key={r}
                            onClick={() => onRoleChange(r)}
                            className="text-left px-2 py-1.5 rounded-md transition-all"
                            style={{
                                fontSize: 12,
                                background: role === r ? `${roleColors[r]}22` : "transparent",
                                color: role === r ? roleColors[r] : "#64748B",
                                border: role === r ? `1px solid ${roleColors[r]}44` : "1px solid transparent",
                                fontWeight: role === r ? 600 : 400,
                            }}
                        >
                            {roleLabels[r]}
                        </button>
                    ))}
                </div>
            </div>

            {/* Current role badge */}
            <div className="px-3 pb-2">
                <div
                    className="px-3 py-2 rounded-lg flex items-center gap-2"
                    style={{ background: `${roleColors[role]}18` }}
                >
                    <div className="w-1.5 h-1.5 rounded-full" style={{ background: roleColors[role] }} />
                    <span style={{ fontSize: 13, color: roleColors[role], fontWeight: 600 }}>{roleLabels[role]}</span>
                </div>
            </div>

            {/* Nav */}
            <nav className="flex-1 px-3 py-2 overflow-y-auto">
                <div style={{ fontSize: 15, color: "#64748B", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6, paddingLeft: 8 }}>
                    메뉴
                </div>
                <div className="flex flex-col gap-0.5">
                    {items.map((item) => {
                        const active = item.screen === screen;
                        return (
                            <button
                                key={item.screen}
                                onClick={() => onScreenChange(item.screen)}
                                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-all w-full group"
                                style={{
                                    background: active ? "rgba(37,99,235,0.15)" : "transparent",
                                    color: active ? "#60A5FA" : "#94A3B8",
                                    borderLeft: active ? "2px solid #2563EB" : "2px solid transparent",
                                }}
                            >
                                <span style={{ color: active ? "#60A5FA" : "#64748B" }}>{item.icon}</span>
                                <span style={{ fontSize: 15, fontWeight: active ? 600 : 400 }}>{item.label}</span>
                                {active && <ChevronRight size={14} className="ml-auto" style={{ color: "#60A5FA" }} />}
                            </button>
                        );
                    })}
                </div>
            </nav>

            {/* Bottom */}
            <div className="px-3 pb-4" style={{ borderTop: "1px solid rgba(255,255,255,0.06)", paddingTop: 12 }}>
                <button className="flex items-center gap-3 px-3 py-2.5 rounded-lg w-full transition-all group" style={{ color: "#64748B" }}>
                    <Bell size={18} />
                    <span style={{ fontSize: 15 }}>알림</span>
                    {notifications > 0 && (
                        <span className="ml-auto px-1.5 py-0.5 rounded-full" style={{ background: "#DC2626", color: "#fff", fontSize: 11, fontWeight: 700 }}>
              {notifications}
            </span>
                    )}
                </button>
                <button className="flex items-center gap-3 px-3 py-2.5 rounded-lg w-full transition-all" style={{ color: "#64748B" }}>
                    <Settings size={18} />
                    <span style={{ fontSize: 15 }}>설정</span>
                </button>
                <div className="flex items-center gap-2 px-3 py-2 mt-1 rounded-lg" style={{ background: "rgba(255,255,255,0.04)" }}>
                    <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ background: "#334155", color: "#94A3B8", fontSize: 13, fontWeight: 700 }}>
                        김
                    </div>
                    <div className="flex-1 min-w-0">
                        <div style={{ fontSize: 14, color: "#CBD5E1", fontWeight: 500 }}>김민준</div>
                        <div style={{ fontSize: 11, color: "#475569" }}>민트커피 강남점</div>
                    </div>
                    <LogOut size={15} style={{ color: "#475569" }} />
                </div>
            </div>
        </aside>
    );
}