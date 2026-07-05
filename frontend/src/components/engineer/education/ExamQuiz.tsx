// src/components/engineer/education/ExamQuiz.tsx
import { useState } from "react";
import type { Exam, ExamResult } from "../../../api/education";

interface Props {
  exam: Exam;
  submitting: boolean;
  result: ExamResult | null;
  onSubmit: (answers: { questionId: number; answer: string }[]) => void;
  onRetry: () => void;
}

export default function ExamQuiz({ exam, submitting, result, onSubmit, onRetry }: Props) {
  const [answers, setAnswers] = useState<Record<number, string>>({});

  const allAnswered = exam.questions.every((q) => answers[q.questionId] !== undefined);

  const handleSelect = (questionId: number, choiceIndex: number) => {
    setAnswers((prev) => ({ ...prev, [questionId]: String(choiceIndex + 1) }));
  };

  const handleSubmit = () => {
    const payload = exam.questions.map((q) => ({
      questionId: q.questionId,
      answer: answers[q.questionId],
    }));
    onSubmit(payload);
  };

  if (result) {
    const okColor = result.passed ? "#22C55E" : "#F87171";
    return (
      <div
        className="rounded-xl"
        style={{
          background: "var(--card)",
          border: `1px solid ${okColor}44`,
          padding: "1.5rem",
          textAlign: "center",
        }}
      >
        <h2 style={{ margin: "0 0 8px", color: okColor, fontWeight: 700 }}>
          {result.passed ? "합격했습니다 🎉" : "불합격"}
        </h2>
        <p style={{ margin: "0 0 4px", color: "#CBD5E1" }}>
          점수: <strong style={{ color: "#0F172A" }}>{result.score}</strong> / 합격 기준 {result.passScore}점
        </p>
        {result.gradeUpgraded && (
          <p style={{ margin: "4px 0", color: "#8B5CF6", fontWeight: 600 }}>
            기술 등급이 {result.newGrade}로 상향되었습니다!
          </p>
        )}
        {!result.passed && (
          <button
            onClick={onRetry}
            style={{
              marginTop: 16,
              padding: "10px 20px",
              borderRadius: 8,
              border: "none",
              background: "var(--primary)",
              color: "#fff",
              cursor: "pointer",
              fontWeight: 600,
            }}
          >
            재응시하기
          </button>
        )}
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      {exam.questions.map((q, idx) => (
        <div
          key={q.questionId}
          className="rounded-xl"
          style={{
            background: "var(--card)",
            border: "1px solid var(--border)",
            padding: "1rem 1.25rem",
          }}
        >
          <p style={{ margin: "0 0 12px", fontSize: 14, color: "#0F172A", fontWeight: 500 }}>
            {idx + 1}. {q.question} <span style={{ color: "#64748B", fontSize: 12 }}>({q.score}점)</span>
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {q.choices.map((choice, choiceIdx) => {
              const selected = answers[q.questionId] === String(choiceIdx + 1);
              return (
                <label
                  key={choiceIdx}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    padding: "8px 10px",
                    borderRadius: 8,
                    cursor: "pointer",
                    background: selected ? "rgba(37,99,235,0.15)" : "transparent",
                    border: selected ? "1px solid var(--primary)" : "1px solid var(--border)",
                    fontSize: 13,
                    color: selected ? "#0F172A" : "#CBD5E1",
                  }}
                >
                  <input
                    type="radio"
                    name={`q-${q.questionId}`}
                    checked={selected}
                    onChange={() => handleSelect(q.questionId, choiceIdx)}
                  />
                  {choice}
                </label>
              );
            })}
          </div>
        </div>
      ))}

      <button
        disabled={!allAnswered || submitting}
        onClick={handleSubmit}
        style={{
          padding: "12px 20px",
          borderRadius: 8,
          border: "none",
          background: allAnswered ? "var(--primary)" : "rgba(255,255,255,0.08)",
          color: allAnswered ? "#fff" : "#64748B",
          cursor: allAnswered ? "pointer" : "not-allowed",
          fontWeight: 600,
        }}
      >
        {submitting ? "채점 중..." : "제출하기"}
      </button>
    </div>
  );
}
