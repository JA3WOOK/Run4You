// src/hooks/useCourseDetail.ts
import { useEffect, useState, useCallback } from "react";
import { fetchCourseDetail, updateLessonProgress } from "../api/education";
import type { CourseDetail } from "../api/education";

export function useCourseDetail(accessToken: string, courseId: number) {
  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(() => {
    if (!accessToken) return;
    setLoading(true);
    setError(null);
    fetchCourseDetail(accessToken, courseId)
      .then(setCourse)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [accessToken, courseId]);

  useEffect(() => {
    reload();
  }, [reload]);

  /** 동영상 플레이어에서 주기적으로(예: 5~10초마다) 호출 */
  const reportProgress = useCallback(
    async (lessonId: number, watchedSeconds: number) => {
      const result = await updateLessonProgress(accessToken, lessonId, watchedSeconds);
      setCourse((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          myProgressRate: result.courseProgressRate,
          myStatus: result.courseStatus,
          lessons: prev.lessons.map((l) =>
            l.lessonId === lessonId
              ? {
                  ...l,
                  watchedSeconds,
                  progressRate: result.lessonProgressRate,
                  completed: result.lessonCompleted,
                }
              : l
          ),
        };
      });
      return result;
    },
    [accessToken]
  );

  return { course, loading, error, reload, reportProgress };
}
