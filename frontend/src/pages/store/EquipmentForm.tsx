import { useState } from "react";
import { X } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { registerEquipment } from "../../api/equipment";
import type { EquipmentCreateRequest } from "../../api/equipment";

const categories = [
    { value: "KIOSK", label: "키오스크" },
    { value: "ESPRESSO", label: "에스프레소" },
    { value: "ICE_MAKER", label: "제빙기" },
    { value: "REFRIGERATOR", label: "냉장고" },
] as const;

interface Props {
    onClose: () => void;
    onSuccess: () => void;
}

export function EquipmentForm({ onClose, onSuccess }: Props) {
    const { accessToken } = useAuth();
    const [form, setForm] = useState<Omit<EquipmentCreateRequest, "purchasedAt">>({
        name: "",
        category: "KIOSK",
        modelName: "",
        manufacturer: "",
        serialNo: "",
    });
    const [purchasedAt, setPurchasedAt] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const update = (key: keyof typeof form, value: string) =>
        setForm((prev) => ({ ...prev, [key]: value }));

    const handleSubmit = async () => {
        if (!form.name || !form.modelName || !form.manufacturer || !form.serialNo) {
            setError("필수 항목을 모두 입력해주세요.");
            return;
        }
        if (!accessToken) return;

        setLoading(true);
        setError("");
        try {
            await registerEquipment(accessToken, {
                ...form,
                purchasedAt: purchasedAt || undefined,
            });
            onSuccess();
            onClose();
        } catch (err) {
            console.error(err);
            setError("기자재 등록에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            style={{ background: "rgba(15,23,42,0.4)" }}
            onClick={onClose}
        >
            <div
                className="rounded-2xl w-full max-w-md"
                style={{ background: "#fff", boxShadow: "0 20px 50px rgba(0,0,0,0.25)" }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* 헤더 */}
                <div className="flex items-start justify-between px-7 pt-7 pb-5">
                    <div>
                        <h2 style={{ fontSize: 20, fontWeight: 700, color: "#0F172A" }}>기자재 등록</h2>
                        <p style={{ fontSize: 14, color: "#64748B", marginTop: 4 }}>
                            매장에서 사용하는 기자재 정보를 입력해주세요.
                        </p>
                    </div>
                    <button onClick={onClose} className="mt-1">
                        <X size={22} style={{ color: "#94A3B8" }} />
                    </button>
                </div>

                {/* 폼 본문 */}
                <div className="px-7 pb-2">
                    {/* 1행: 기기명 + 카테고리 */}
                    <div className="grid grid-cols-2 gap-3 mb-4">
                        <Field label="기자재명 (별칭)" required>
                            <input value={form.name} onChange={(e) => update("name", e.target.value)} placeholder="예) 키오스크 2호기" style={inputStyle} />
                        </Field>
                        <Field label="카테고리" required>
                            <select value={form.category} onChange={(e) => update("category", e.target.value)} style={inputStyle}>
                                {categories.map((c) => (
                                    <option key={c.value} value={c.value}>{c.label}</option>
                                ))}
                            </select>
                        </Field>
                    </div>

                    {/* 2행: 모델명 + 제조사 */}
                    <div className="grid grid-cols-2 gap-3 mb-4">
                        <Field label="모델명" required>
                            <input value={form.modelName} onChange={(e) => update("modelName", e.target.value)} placeholder="예) POSBANK PKR-20" style={inputStyle} />
                        </Field>
                        <Field label="제조사" required>
                            <input value={form.manufacturer} onChange={(e) => update("manufacturer", e.target.value)} placeholder="예) POSBANK" style={inputStyle} />
                        </Field>
                    </div>

                    {/* 3행: 시리얼 번호 (전체 너비) */}
                    <div className="mb-4">
                        <Field label="시리얼 번호" required>
                            <input value={form.serialNo} onChange={(e) => update("serialNo", e.target.value)} placeholder="예) PKR20-A1234" style={inputStyle} />
                        </Field>
                    </div>

                    {/* 4행: 구입일 (직접 입력, 절반 너비) */}
                    <div className="mb-2" style={{ width: "50%" }}>
                        <Field label="구입일">
                            <input
                                value={purchasedAt}
                                onChange={(e) => setPurchasedAt(e.target.value)}
                                placeholder="2023-04-12"
                                style={inputStyle}
                            />
                        </Field>
                    </div>

                    {error && <p style={{ fontSize: 13, color: "#DC2626", marginTop: 8 }}>{error}</p>}
                </div>

                {/* 하단 버튼 */}
                <div className="flex justify-end gap-2 px-7 py-6">
                    <button onClick={onClose} className="px-5 py-2.5 rounded-lg transition-all" style={{ background: "#fff", border: "1px solid rgba(15,23,42,0.12)", color: "#475569", fontSize: 14, fontWeight: 600 }}>
                        취소
                    </button>
                    <button onClick={handleSubmit} disabled={loading} className="px-6 py-2.5 rounded-lg transition-all" style={{ background: "#2563EB", color: "#fff", fontSize: 14, fontWeight: 600, opacity: loading ? 0.7 : 1 }}>
                        {loading ? "등록 중..." : "등록"}
                    </button>
                </div>
            </div>
        </div>
    );
}

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
    return (
        <div className="flex flex-col gap-2">
            <label style={{ fontSize: 14, fontWeight: 600, color: "#334155" }}>
                {label}
                {required && <span style={{ color: "#DC2626", marginLeft: 3 }}>*</span>}
            </label>
            {children}
        </div>
    );
}

const inputStyle: React.CSSProperties = {
    width: "100%",
    padding: "11px 14px",
    borderRadius: 10,
    border: "1px solid rgba(15,23,42,0.12)",
    fontSize: 14,
    color: "#0F172A",
    outline: "none",
    background: "#F8FAFC",
};