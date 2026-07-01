import { useState, useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import SuperAdminBrandsPage from "./pages/SuperAdminBrandsPage";
import SuperAdminUsersPage from "./pages/SuperAdminUsersPage";
import { Sidebar } from "./components/layout/Sidebar";
import type { UserRole, Screen } from "./components/layout/Sidebar";
import { Header } from "./components/layout/Header";
import { ToastNotification, type ToastView } from "./components/common/ToastNotification";
import { StoreHome } from "./pages/store/StoreHome";
import { StoreReceipt } from "./pages/store/StoreReceipt";
import { StoreASForm } from "./pages/store/StoreASForm";
import { StoreDispatch } from "./pages/store/StoreDispatch";
import { EngQueue } from "./components/engineer/EngQueue";
import { EngDetail } from "./components/engineer/EngDetail";
import { EngStatus } from "./pages/engineer/EngStatus";
import BrandAdminUsersPage from "./pages/BrandAdminUsersPage";
import { EngReport } from "./components/engineer/EngReport";
import { AdminBilling } from "./components/admin/AdminBilling";
import { AdminStats } from "./components/admin/AdminStats";
import { AdminDispatchControl } from "./components/admin/AdminDispatchControl";
import { SuperDashboard } from "./components/super/SuperDashboard";
import { SettingsPage } from "./pages/SettingsPage";
import { subscribeDispatch } from "./api/dispatch";
import { getMyNotifications } from "./api/notification";

const screenLabels: Record<string, string> = {
  "store-home": "기자재 현황",
  "store-as-form": "긴급 A/S 접수",
  "store-dispatch": "출동 현황",
  "store-receipt": "진단서 / 영수증",
  "eng-queue": "출동 요청 대기열",
  "eng-detail": "출동 상세",
  "eng-status": "수리 상태 변경",
  "eng-report": "정비 리포트",
  "admin-dashboard": "통합 관제 대시보드",
  "admin-equipment": "기자재 관리",
  "admin-billing": "정산 관리",
  "admin-users": "회원 승인 관리",
  "super-dashboard": "전체 통계 대시보드",
  "super-brands": "브랜드 관리",
  "super-users": "회원 관리",
  "settings": "설정",
};

const defaultScreen: Record<UserRole, Screen> = {
  STORE_OWNER: "store-home",
  ENGINEER: "eng-queue",
  BRAND_ADMIN: "admin-dashboard",
  SUPER_ADMIN: "super-dashboard",
};

function Dashboard() {
  const { user, signOut, accessToken } = useAuth();
  const role = (user?.role ?? "STORE_OWNER") as UserRole;
  const [screen, setScreen] = useState<Screen>(defaultScreen[role]);
  const [selectedAsRequestId, setSelectedAsRequestId] = useState<number | null>(null);
  const [acceptedAssignmentId, setAcceptedAssignmentId] = useState<number | null>(null);
  const [trackAssignmentId, setTrackAssignmentId] = useState<number | null>(null);
  const [trackEngineer, setTrackEngineer] = useState<{ name: string | null; phone: string | null }>({ name: null, phone: null });
  const [unreadCount, setUnreadCount] = useState(0);
  const [toasts, setToasts] = useState<ToastView[]>([]);
  const [sseConnected, setSseConnected] = useState(false);

  // 최초 미읽음 수 로드
  useEffect(() => {
    if (!accessToken) return;
    getMyNotifications(accessToken)
        .then((r) => setUnreadCount(r.unreadCount))
        .catch((e) => console.warn("알림 미읽음 수 로드 실패:", e));
  }, [accessToken]);

  // 앱 레벨 SSE — notification 이벤트로 토스트 + 배지 갱신
  useEffect(() => {
    if (!accessToken) return;
    const unsubscribe = subscribeDispatch(accessToken, {
      onConnected: () => setSseConnected(true),
      onNotification: (n) => {
        setUnreadCount((c) => c + 1);
        const id = Date.now() + Math.floor(Math.random() * 1000);
        setToasts((prev) => [...prev, { id, type: n.type, title: n.title, message: n.message }]);
        setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 5000);
      },
      onError: (e) => { setSseConnected(false); console.warn("[SSE/알림] 재연결 시도 중...", e); },
    });
    return unsubscribe;
  }, [accessToken]);

  const dismissToast = (id: number) => setToasts((prev) => prev.filter((t) => t.id !== id));

  const handleScreenChange = (s: Screen) => {
    if (s === "eng-queue") setSelectedAsRequestId(null);
    setScreen(s);
  };

  return (
      <div
          className="flex h-screen overflow-hidden"
          style={{ background: "var(--background)", fontFamily: "var(--font-sans)" }}
      >
        <Sidebar
            role={role}
            screen={screen}
            onScreenChange={handleScreenChange}
            onRoleChange={() => {}}
            notifications={unreadCount}
            userName={user?.name ?? ''}
            onLogout={signOut}
        />
        <main className="flex-1 overflow-y-auto">
          <Header screenLabel={screenLabels[screen] ?? screen} sseConnected={sseConnected} />
          <div className="px-8 py-6">

            {/* ── 점주 ── */}
            {screen === "store-home" && (
                <StoreHome
                    onRequestAS={() => setScreen("store-as-form")}
                    onGoReceipts={() => setScreen("store-receipt")}
                    onTrack={(assignmentId, engineer) => {
                      setTrackAssignmentId(assignmentId);
                      setTrackEngineer({ name: engineer?.name ?? null, phone: engineer?.phone ?? null });
                      setScreen("store-dispatch");
                    }}
                />
            )}
            {screen === "store-as-form" && <StoreASForm onComplete={() => setScreen("store-home")} />}
            {screen === "store-receipt" && <StoreReceipt />}
            {screen === "store-dispatch" && (
                trackAssignmentId != null ? (
                    <StoreDispatch
                        assignmentId={trackAssignmentId}
                        engineerName={trackEngineer.name}
                        engineerPhone={trackEngineer.phone}
                    />
                ) : (
                    <div
                        className="rounded-xl p-8 text-center"
                        style={{ background: "var(--card)", border: "1px solid var(--border)", color: "var(--muted-foreground)", fontSize: 14 }}
                    >
                      추적할 출동 건이 없습니다. 기자재 현황의 "진행 중 A/S"에서 추적을 눌러 진입해 주세요.
                    </div>
                )
            )}

            {/* ── 엔지니어 ── */}
            {screen === "eng-queue" && (
                <EngQueue
                    onSelect={(asRequestId) => {
                      setSelectedAsRequestId(asRequestId);
                      setScreen("eng-detail");
                    }}
                />
            )}
            {screen === "eng-detail" && selectedAsRequestId && (
                <EngDetail
                    asRequestId={selectedAsRequestId}
                    onBack={() => {
                      setSelectedAsRequestId(null);
                      setScreen("eng-queue");
                    }}
                    onAccepted={(assignmentId) => {
                      setAcceptedAssignmentId(assignmentId);
                      setSelectedAsRequestId(null);
                      setScreen("eng-status");
                    }}
                />
            )}
            {screen === "eng-status" && (
                acceptedAssignmentId != null ? (
                    <EngStatus
                        assignmentId={acceptedAssignmentId}
                        onComplete={() => setScreen("eng-queue")}
                    />
                ) : (
                    <div
                        className="rounded-xl p-8 text-center"
                        style={{ background: "var(--card)", border: "1px solid var(--border)", color: "var(--muted-foreground)", fontSize: 14 }}
                    >
                      수락한 출동 건이 없습니다. "출동 요청 대기열"에서 먼저 수락해 주세요.
                    </div>
                )
            )}
            {screen === "eng-report" && (
                <EngReport
                    assignmentId={acceptedAssignmentId ?? Date.now()}
                    asRequestId={selectedAsRequestId ?? 1}
                    engineerId={4}
                    equipmentId={1}
                    onSubmit={() => setScreen("eng-queue")}
                />
            )}

            {/* ── 본사 관리자 ── */}
            {screen === "admin-dashboard" && (
                <div className="flex flex-col gap-6">
                    <AdminDispatchControl />
                    <AdminStats />
                </div>
            )}
            {screen === "admin-billing" && <AdminBilling />}
            {screen === "admin-users" && <BrandAdminUsersPage />}

            {/* ── 공통 ── */}
            {screen === "settings" && <SettingsPage />}

            {/* ── 플랫폼 총괄 ── */}
            {screen === "super-dashboard" && <SuperDashboard />}
            {screen === "super-brands" && <SuperAdminBrandsPage />}
            {screen === "super-users" && <SuperAdminUsersPage />}

          </div>
        </main>
        <ToastNotification toasts={toasts} onDismiss={dismissToast} />
      </div>
  );
}

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  return user ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
  );
}
