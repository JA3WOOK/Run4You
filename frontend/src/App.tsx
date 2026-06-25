import { useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import SuperAdminBrandsPage from "./pages/SuperAdminBrandsPage";
import SuperAdminUsersPage from "./pages/SuperAdminUsersPage";
import { Sidebar } from "./components/layout/Sidebar";
import type { UserRole, Screen } from "./components/layout/Sidebar";
import { Header } from "./components/layout/Header";
import { ToastNotification } from "./components/common/ToastNotification";

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
  "super-dashboard": "전체 통계 대시보드",
  "super-brands": "브랜드 관리",
  "super-users": "회원 관리",
};

const defaultScreen: Record<UserRole, Screen> = {
  STORE_OWNER: "store-home",
  ENGINEER: "eng-queue",
  BRAND_ADMIN: "admin-dashboard",
  SUPER_ADMIN: "super-dashboard",
};

function Dashboard() {
  const { user, signOut } = useAuth();
  const role = (user?.role ?? "STORE_OWNER") as UserRole;
  const [screen, setScreen] = useState<Screen>(defaultScreen[role]);

  return (
    <div
      className="flex h-screen overflow-hidden"
      style={{ background: "var(--background)", fontFamily: "var(--font-sans)" }}
    >
      <Sidebar
        role={role}
        screen={screen}
        onScreenChange={setScreen}
        onRoleChange={() => {}}
        notifications={3}
        userName={user?.name ?? ''}
        onLogout={signOut}
      />
      <main className="flex-1 overflow-y-auto">
        <Header screenLabel={screenLabels[screen] ?? screen} currentTime="2026-06-15 14:32" />
        <div className="px-8 py-6">
          {screen === "super-brands" && <SuperAdminBrandsPage />}
          {screen === "super-users" && <SuperAdminUsersPage />}
        </div>
      </main>
      <ToastNotification />
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
