import { useState } from "react";
import { useCourses } from "../../hooks/useCourses";
import type { CourseLevel } from "../../api/education";
import CourseCard from "../../components/engineer/education/CourseCard";

interface Props {
  accessToken: string;
  onOpenCourse: (courseId: number) => void;
}

const TABS: { label: string; value?: CourseLevel }[] = [
  { label: "전체", value: undefined },
  { label: "초급", value: "BEGINNER" },
  { label: "중급", value: "INTERMEDIATE" },
  { label: "고급", value: "ADVANCED" },
];

export default function EngineerCourseListPage({ accessToken, onOpenCourse }: Props) {
  const [levelFilter, setLevelFilter] = useState<CourseLevel | undefined>(undefined);
  const { courses, loading, error } = useCourses(accessToken, levelFilter);

  return (
    <div>
      <h1 style={{ fontSize: 20, color: "#0F172A", marginBottom: 4, fontWeight: 700 }}>기술 등급 코스</h1>
      <p style={{ fontSize: 13, color: "#94A3B8", marginBottom: 20 }}>
        초급·중급·고급 코스를 수강하고 필기시험에 합격하면 기술 등급이 상향됩니다.
      </p>

      <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
        {TABS.map((tab) => (
          <button
            key={tab.label}
            onClick={() => setLevelFilter(tab.value)}
            style={{
              padding: "6px 14px",
              borderRadius: 20,
              border: levelFilter === tab.value ? "1px solid var(--primary)" : "1px solid var(--border)",
              background: levelFilter === tab.value ? "var(--primary)" : "transparent",
              color: levelFilter === tab.value ? "#fff" : "#94A3B8",
              cursor: "pointer",
              fontSize: 13,
              fontWeight: levelFilter === tab.value ? 600 : 400,
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading && <p style={{ color: "#94A3B8" }}>불러오는 중...</p>}
      {error && <p style={{ color: "#F87171" }}>{error}</p>}

      {!loading && !error && courses.length === 0 && (
        <p style={{ color: "#94A3B8" }}>등록된 코스가 없습니다.</p>
      )}

      {courses.map((course) => (
        <CourseCard key={course.courseId} course={course} onClick={onOpenCourse} />
      ))}
    </div>
  );
}
