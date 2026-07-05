// src/hooks/useExam.ts
import { useEffect, useState, useCallback } from "react";
import { fetchExam, submitExam } from "../api/education";
import type { Exam, ExamResult } from "../api/education";

export function useExam(accessToken: string, courseId: number) {
  const [exam, setExam] = useState<Exam | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ExamResult | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!accessToken) return;
    setLoading(true);
    setError(null);
    fetchExam(accessToken, courseId)
      .then(setExam)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [accessToken, courseId]);

  const submit = useCallback(
    async (answers: { questionId: number; answer: string }[]) => {
      if (!exam) return;
      setSubmitting(true);
      setError(null);
      try {
        const r = await submitExam(accessToken, exam.examId, answers);
        setResult(r);
        return r;
      } catch (e: any) {
        setError(e.message);
      } finally {
        setSubmitting(false);
      }
    },
    [accessToken, exam]
  );

  return { exam, loading, error, result, submitting, submit };
}
