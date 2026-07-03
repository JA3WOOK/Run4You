import { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { fetchMyActiveAssignment } from "../../api/matching";
import { EngQueue } from "../../components/engineer/EngQueue";
import { EngDetail } from "../../components/engineer/EngDetail";
import { EngStatus } from "../../components/engineer/EngStatus";

type View =
    | { type: "loading" }
    | { type: "queue" }
    | { type: "detail"; asRequestId: number }
    | { type: "accepted"; assignmentId: number };

export default function EngineerPage() {
    const { accessToken } = useAuth();
    const [view, setView] = useState<View>({ type: "loading" });

    useEffect(() => {
        fetchMyActiveAssignment(accessToken)
            .then((active) =>
                setView(active ? { type: "accepted", assignmentId: active.assignmentId } : { type: "queue" })
            )
            .catch(() => setView({ type: "queue" }));
    }, [accessToken]);

    if (view.type === "loading") {
        return <div className="p-6 text-sm text-slate-500">불러오는 중...</div>;
    }

    if (view.type === "queue") {
        return <EngQueue onSelect={(id) => setView({ type: "detail", asRequestId: id })} />;
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

    return (
        <EngStatus
            assignmentId={view.assignmentId}
            onComplete={() => { /* 리포트 화면 이동 처리 */ }}
        />
    );
}