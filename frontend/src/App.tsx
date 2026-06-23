import { useState } from "react";
import { Sidebar } from "./components/layout/Sidebar";
import type { UserRole, Screen } from "./components/layout/Sidebar";
import { Header } from "./components/layout/Header";
import { ToastNotification } from "./components/common/ToastNotification";

/* 테스트용 임시 구현 */

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
};

// 역할별 기본 화면 (전환 시 그 역할의 첫 메뉴로 이동)
const defaultScreen: Record<UserRole, Screen> = {
  STORE_OWNER: "store-home",
  ENGINEER: "eng-queue",
  BRAND_ADMIN: "admin-dashboard",
  SUPER_ADMIN: "super-dashboard",
};

export default function App() {
  const [role, setRole] = useState<UserRole>("STORE_OWNER");
  const [screen, setScreen] = useState<Screen>("store-home");

  // 역할 바뀌면 그 역할의 첫 화면으로 이동
  const handleRoleChange = (r: UserRole) => {
    setRole(r);
    setScreen(defaultScreen[r]);
  };

  return (
      <div
          className="flex h-screen overflow-hidden"
          style={{ background: "var(--background)", fontFamily: "var(--font-sans)" }}
      >
        <Sidebar
            role={role}
            screen={screen}
            onScreenChange={setScreen}
            onRoleChange={handleRoleChange}
            notifications={3}
        />

        <main className="flex-1 overflow-y-auto">
          <Header screenLabel={screenLabels[screen] ?? screen} currentTime="2026-06-15 14:32" />

          <div className="px-8 py-6">
            {/* 여기에 store 화면들이 들어갈 자리 */}
          </div>
        </main>

        <ToastNotification />
      </div>
  );
}