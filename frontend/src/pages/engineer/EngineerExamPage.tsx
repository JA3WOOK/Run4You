// src/pages/engineer/EngineerExamPage.tsx
import { useExam } from "../../hooks/useExam";
import ExamQuiz from "../../components/engineer/education/ExamQuiz";

interface Props {
  accessToken: string;
  courseId: number;
  onBack: () => void;
}

export default function EngineerExamPage({ accessToken, courseId, onBack }: Props) {
  const { exam, loading, error, result, submitting, submit } = useExam(accessToken, courseId);

  return (
    <div style={{ maxWidth: 720 }}>
      <button
        onClick={onBack}
        style={{ background: "none", border: "none", color: "#94A3B8", cursor: "pointer", marginBottom: 12, fontSize: 13 }}
      >
        ← 코스로 돌아가기
      </button>

      {loading && <p style={{ color: "#94A3B8" }}>불러오는 중...</p>}
      {error && <p style={{ color: "#F87171" }}>{error}</p>}

      {exam && (
        <>
          <h1 style={{ fontSize: 18, color: "#0F172A", marginBottom: 4, fontWeight: 700 }}>{exam.title}</h1>
          <p style={{ fontSize: 13, color: "#94A3B8", marginBottom: 20 }}>
            합격 기준: {exam.passScore}점 / 총 {exam.totalScore}점
          </p>
          <ExamQuiz
            exam={exam}
            submitting={submitting}
            result={result}
            onSubmit={submit}
            onRetry={onBack}
          />
        </>
      )}
    </div>
  );
}
