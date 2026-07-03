import {useState, useEffect} from "react";
import {X, AlertCircle, Zap, CheckCircle} from "lucide-react";
import {useAuth} from "../../context/AuthContext";
import {getActiveAsRequestByEquipment, cancelAsRequest} from "../../api/asRequest";
import type {AsRequestResponse} from "../../api/asRequest";

const statusLabels: Record<string, string> = {
    RECEIVED: "접수완료",
    MATCHING: "엔지니어 매칭 중",
    ASSIGNED: "엔지니어 배정됨",
    IN_PROGRESS: "수리 진행 중",
    COMPLETED: "수리 완료",
    CANCELLED: "취소됨",
};

export function AsRequestDetailModal({
                                         equipmentId,
                                         equipmentName,
                                         onClose,
                                         onCancelled,
                                     }: {
    equipmentId: number;
    equipmentName: string;
    onClose: () => void;
    // 취소 성공 시 부모(StoreHome)에서 목록 리페치하도록 알림
    onCancelled?: () => void;
}) {
    const {accessToken} = useAuth();
    const [data, setData] = useState<AsRequestResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [canceling, setCanceling] = useState(false);

    useEffect(() => {
        if (!accessToken) return;
        setLoading(true);
        getActiveAsRequestByEquipment(accessToken, equipmentId)
            .then((res) => {
                setData(res);
                setError("");
            })
            .catch((err) => {
                console.error(err);
                setError("접수 내용을 불러오지 못했습니다.");
            })
            .finally(() => setLoading(false));
    }, [accessToken, equipmentId]);

    // 배정 전(RECEIVED)에만 취소 가능 — 백엔드 AsRequest.cancel() 조건과 동일
    const cancellable = data?.status === "RECEIVED";

    const handleCancel = async () => {
        if (!accessToken || !data) return;
        if (!window.confirm("이 접수를 취소하시겠습니까?")) return;

        setCanceling(true);
        try {
            await cancelAsRequest(accessToken, data.id);
            onCancelled?.();
            onClose();
        } catch (err) {
            console.error("A/S 취소 실패:", err);
            alert("접수 취소에 실패했습니다. 이미 엔지니어가 배정되었을 수 있습니다.");
        } finally {
            setCanceling(false);
        }
    };

    return (
        <div
            className="fixed inset-0 flex items-center justify-center z-50"
            style={{background: "rgba(15,23,42,0.5)"}}
            onClick={onClose}
        >
            <div
                className="rounded-2xl p-6 flex flex-col gap-5"
                style={{background: "#fff", width: 480, maxWidth: "90vw", maxHeight: "85vh", overflowY: "auto"}}
                onClick={(e) => e.stopPropagation()}
            >
                {/* Header */}
                <div className="flex items-start justify-between">
                    <div>
                        <h2 style={{fontSize: 18, fontWeight: 700, color: "#0F172A"}}>A/S 접수 내용</h2>
                        <p style={{fontSize: 13, color: "#64748B", marginTop: 2}}>{equipmentName}</p>
                    </div>
                    <button onClick={onClose} className="p-1 rounded-lg hover:bg-slate-100">
                        <X size={20} style={{color: "#94A3B8"}}/>
                    </button>
                </div>

                {loading && <p style={{color: "#94A3B8", fontSize: 14}}>불러오는 중...</p>}
                {error && <p style={{color: "#DC2626", fontSize: 14}}>{error}</p>}

                {!loading && !error && data && (
                    <div className="flex flex-col gap-4">
                        {/* 접수번호 + 상태 */}
                        <div className="flex items-center justify-between px-4 py-3 rounded-xl"
                             style={{background: "#F8FAFC"}}>
                            <span style={{fontSize: 14, color: "#64748B"}}>접수번호 AS-{data.id}</span>
                            <span className="px-3 py-1 rounded-full"
                                  style={{background: "#EFF6FF", fontSize: 12, color: "#2563EB", fontWeight: 600}}>
                                {statusLabels[data.status] ?? data.status}
                            </span>
                        </div>

                        {/* 우선순위 */}
                        <div className="flex items-center gap-3 px-4 py-3 rounded-xl"
                             style={{background: data.priority === "EMERGENCY" ? "#FEF2F2" : "#EFF6FF"}}>
                            <div className="w-9 h-9 rounded-lg flex items-center justify-center"
                                 style={{background: data.priority === "EMERGENCY" ? "#DC2626" : "#2563EB"}}>
                                {data.priority === "EMERGENCY" ? <Zap size={16} color="#fff"/> :
                                    <CheckCircle size={16} color="#fff"/>}
                            </div>
                            <div>
                                <div style={{
                                    fontSize: 14,
                                    fontWeight: 700,
                                    color: data.priority === "EMERGENCY" ? "#DC2626" : "#2563EB"
                                }}>
                                    {data.priority === "EMERGENCY" ? "EMERGENCY" : "NORMAL"}
                                </div>
                                <div style={{fontSize: 13, color: "#64748B"}}>
                                    {data.priority === "EMERGENCY" ? "즉각 대응 · 30분 내 배정" : "일반 처리 · 2시간 내 배정"}
                                </div>
                            </div>
                        </div>

                        {/* 에러코드 */}
                        {data.errorCode && data.errorCode.trim() !== "" && (
                            <div className="flex items-center gap-2 px-4 py-3 rounded-xl" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.1)", borderLeft: "5px solid #DC2626" }}>
                                <AlertCircle size={15} style={{ color: "#DC2626", flexShrink: 0 }} />
                                <span style={{ fontSize: 14, color: "#64748B", fontWeight: 500 }}>에러코드: </span>
                                <span style={{ fontSize: 14, color: "#475569", fontWeight: 600, fontFamily: "var(--font-mono)" }}>{data.errorCode}</span>
                            </div>
                        )}

                        {/* 고장 증상 */}
                        <div className="flex flex-col gap-2">
                            <span style={{fontSize: 13, fontWeight: 600, color: "#374151"}}>고장 증상</span>
                            <div className="px-4 py-3 rounded-xl" style={{
                                background: "#F8FAFC",
                                fontSize: 14,
                                color: "#334155",
                                lineHeight: 1.6,
                                whiteSpace: "pre-wrap"
                            }}>
                                {data.symptom ?? "-"}
                            </div>
                        </div>

                        {/* 접수일시 */}
                        <div className="flex justify-between px-1">
                            <span style={{fontSize: 13, color: "#94A3B8"}}>접수일시</span>
                            <span style={{fontSize: 13, color: "#334155", fontWeight: 500}}>
                                {new Date(data.requestedAt).toLocaleString("ko-KR")}
                            </span>
                        </div>

                        {/* 취소 — 배정 전(RECEIVED)에만 노출 */}
                        {cancellable && (
                            <button
                                onClick={handleCancel}
                                disabled={canceling}
                                className="w-full py-3 rounded-xl transition-all hover:bg-slate-100"
                                style={{
                                    background: "transparent",
                                    color: "#64748B",
                                    fontSize: 13,
                                    fontWeight: 600,
                                    border: "1px solid #E2E8F0",
                                    cursor: canceling ? "not-allowed" : "pointer",
                                    opacity: canceling ? 0.6 : 1,
                                }}
                            >
                                {canceling ? "취소 처리 중..." : "접수 취소"}
                            </button>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
