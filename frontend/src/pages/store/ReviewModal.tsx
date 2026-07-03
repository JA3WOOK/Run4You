// src/pages/store/ReviewModal.tsx
import { useState } from "react";
import { X, Star } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { createReview } from "../../api/asRequest";

interface ReviewModalProps {
    asRequestId: number;
    onClose: () => void;
    onSuccess: () => void;
}

export function ReviewModal({ asRequestId, onClose, onSuccess }: ReviewModalProps) {
    const { accessToken } = useAuth();
    const [rating, setRating] = useState(0);
    const [hoverRating, setHoverRating] = useState(0);
    const [comment, setComment] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async () => {
        if (rating === 0) {
            setError("별점을 선택해주세요.");
            return;
        }
        if (!accessToken) return;

        setSubmitting(true);
        setError("");
        try {
            await createReview(accessToken, {
                asRequestId,
                rating,
                comment: comment.trim() || undefined,
            });
            onSuccess();
        } catch (err) {
            console.error(err);
            setError("평가 등록에 실패했습니다. 다시 시도해주세요.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div
            className="fixed inset-0 flex items-center justify-center z-50"
            style={{ background: "rgba(15,23,42,0.45)" }}
            onClick={onClose}
        >
            <div
                onClick={(e) => e.stopPropagation()}
                className="rounded-2xl p-6 w-full"
                style={{
                    background: "#fff",
                    maxWidth: 420,
                    boxShadow: "0 20px 40px rgba(15,23,42,0.15)",
                }}
            >
                {/* 헤더 */}
                <div className="flex items-center justify-between mb-1">
                    <h2 style={{ fontSize: 18, fontWeight: 800, color: "#0F172A" }}>
                        엔지니어 평가하기
                    </h2>
                    <button
                        onClick={onClose}
                        className="transition-all hover:opacity-60"
                        style={{ color: "#94A3B8" }}
                    >
                        <X size={20} />
                    </button>
                </div>
                <p style={{ fontSize: 13, color: "#64748B", marginBottom: 20 }}>
                    수리 서비스는 어떠셨나요? 평가는 다음 배정에 반영됩니다.
                </p>

                {/* 별점 */}
                <div className="flex items-center justify-center gap-2 mb-5">
                    {[1, 2, 3, 4, 5].map((n) => {
                        const filled = n <= (hoverRating || rating);
                        return (
                            <button
                                key={n}
                                type="button"
                                onClick={() => setRating(n)}
                                onMouseEnter={() => setHoverRating(n)}
                                onMouseLeave={() => setHoverRating(0)}
                                className="transition-transform hover:scale-110"
                                style={{ background: "transparent", border: "none", padding: 2 }}
                            >
                                <Star
                                    size={32}
                                    fill={filled ? "#FBBF24" : "none"}
                                    color={filled ? "#FBBF24" : "#CBD5E1"}
                                    strokeWidth={1.5}
                                />
                            </button>
                        );
                    })}
                </div>

                {/* 코멘트 */}
                <textarea
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    placeholder="엔지니어에 대한 의견을 남겨주세요 (선택)"
                    rows={4}
                    maxLength={255}
                    className="w-full rounded-lg p-3 mb-1 resize-none"
                    style={{
                        border: "1px solid rgba(15,23,42,0.1)",
                        fontSize: 14,
                        color: "#0F172A",
                        outline: "none",
                    }}
                />
                <div className="flex justify-end mb-4">
                    <span style={{ fontSize: 11, color: "#94A3B8" }}>{comment.length}/255</span>
                </div>

                {/* 에러 */}
                {error && (
                    <p style={{ fontSize: 13, color: "#DC2626", marginBottom: 12 }}>{error}</p>
                )}

                {/* 버튼 */}
                <div className="flex gap-2">
                    <button
                        onClick={onClose}
                        disabled={submitting}
                        className="flex-1 py-2.5 rounded-lg transition-all hover:bg-slate-100"
                        style={{
                            fontSize: 14,
                            fontWeight: 600,
                            color: "#475569",
                            border: "1px solid rgba(15,23,42,0.1)",
                        }}
                    >
                        취소
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={submitting}
                        className="flex-1 py-2.5 rounded-lg transition-all hover:opacity-90"
                        style={{
                            fontSize: 14,
                            fontWeight: 700,
                            color: "#fff",
                            background: submitting ? "#93B4F0" : "#2563EB",
                        }}
                    >
                        {submitting ? "등록 중..." : "평가 등록"}
                    </button>
                </div>
            </div>
        </div>
    );
}