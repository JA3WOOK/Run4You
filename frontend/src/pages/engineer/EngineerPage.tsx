import { useState } from "react";
import { EngQueue } from "../../components/engineer/EngQueue";
import { EngDetail } from "../../components/engineer/EngDetail";

type View =
  | { type: "queue" }
  | { type: "detail"; asRequestId: number }
  | { type: "accepted"; assignmentId: number };

export default function EngineerPage() {
  const [view, setView] = useState<View>({ type: "queue" });

  if (view.type === "queue") {
    return (
      <EngQueue
        onSelect={(asRequestId) => setView({ type: "detail", asRequestId })}
      />
    );
  }

  if (view.type === "detail") {
    return (
      <EngDetail
        asRequestId={view.asRequestId}
        onBack={() => setView({ type: "queue" })}
        onAccepted={(assignmentId) => setView({ type: "accepted", assignmentId })}
      />
    );
  }

  // 수락 완료 화면
  return (
    <div className="flex flex-col items-center justify-center h-64 gap-4">
      <div style={{ fontSize: 32 }}>✅</div>
      <div style={{ fontSize: 16, fontWeight: 700, color: "#0F172A" }}>출동 수락 완료</div>
      <div style={{ fontSize: 13, color: "#64748B" }}>배정 ID: {view.type === "accepted" ? view.assignmentId : ""}</div>
      <button
        onClick={() => setView({ type: "queue" })}
        className="px-5 py-2.5 rounded-lg"
        style={{ background: "#2563EB", color: "#fff", fontSize: 13, fontWeight: 600 }}
      >
        대기열로 돌아가기
      </button>
    </div>
  );
}
