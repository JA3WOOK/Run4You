// src/hooks/useCourses.ts
import { useEffect, useState, useCallback } from "react";
import { fetchCourses } from "../api/education";
import type { CourseListItem, CourseLevel } from "../api/education";

export function useCourses(accessToken: string, level?: CourseLevel) {
  const [courses, setCourses] = useState<CourseListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(() => {
    if (!accessToken) return;
    setLoading(true);
    setError(null);
    fetchCourses(accessToken, level)
      .then(setCourses)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [accessToken, level]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { courses, loading, error, reload };
}
