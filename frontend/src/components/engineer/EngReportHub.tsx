import { useState } from "react";
import { FileText, ClipboardList } from "lucide-react";
import { EngReportList } from "./EngReportList";
import { EngReport } from "./EngReport";
import type { PendingReport } from "../../api/matching";

interface ReportTarget {
    assignmentId: number;
    asRequestId: number;
    equipmentId: number;
    engineerId: number;
}

interface Props {
    initialContext: ReportTarget | null;
    onSubmitted: () => void;
}

export function EngReportHub({ initialContext, onSubmitted }: Props) {
    const [mode, setMode] = useState<"form" | "list">(initialContext ? "form" : "list");
    const [target, setTarget] = useState<ReportTarget | null>(initialContext);

    const selectFromList = (item: PendingReport) => {
        setTarget({
            assignmentId: item.assignmentId,
            asRequestId: item.asRequestId,
            equipmentId: item.equipmentId,
            engineerId: item.engineerId,
        });
        setMode("form");
    };

    const tabBtn = (key: "form" | "list", label: string, Icon: typeof FileText) => (
        <button
            onClick={() => setMode(key)}
            className="flex items-center gap-2 px-4 py-2.5 rounded-lg transition-all"
            style={{
                background: mode === key ? "#EFF6FF" : "transparent",
                color: mode === key ? "#2563EB" : "#64748B",
                fontSize: 13,
                fontWeight: mode === key ? 700 : 500,
                border: mode === key ? "1px solid #BFDBFE" : "1px solid transparent",
            }}
        >
            <Icon size={15} />
            {label}
        </button>
    );

    return (
        <div className="flex flex-col gap-5">
            <div className="flex items-center gap-1 p-1 rounded-xl w-fit" style={{ background: "#F1F5F9" }}>
                {tabBtn("form", "리포트 작성", FileText)}
                {tabBtn("list", "리포트 없는 목록", ClipboardList)}
            </div>

            {mode === "list" && <EngReportList onSelect={selectFromList} />}

            {mode === "form" && (
                target ? (
                    <EngReport
                        assignmentId={target.assignmentId}
                        asRequestId={target.asRequestId}
                        engineerId={target.engineerId}
                        equipmentId={target.equipmentId}
                        onSubmit={() => {
                            setTarget(null);
                            onSubmitted();
                            setMode("list");
                        }}
                    />
                ) : (
                    <div
                        className="rounded-xl p-8 text-center"
                        style={{ background: "var(--card)", border: "1px solid var(--border)", color: "var(--muted-foreground)", fontSize: 14 }}
                    >
                        작성할 리포트를 선택해주세요. "리포트 없는 목록" 탭에서 고를 수 있습니다.
                    </div>
                )
            )}
        </div>
    );
}