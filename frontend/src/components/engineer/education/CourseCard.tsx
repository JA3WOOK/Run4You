// src/components/engineer/education/CourseCard.tsx
import type { CourseListItem } from "../../../api/education";
import ProgressBar from "./ProgressBar";
import GradeBadge from "./GradeBadge";

interface Props {
  course: CourseListItem;
  onClick: (courseId: number) => void;
}

const STATUS_LABEL: Record<string, string> = {
  NOT_STARTED: "미수강",
  IN_PROGRESS: "수강중",
  COMPLETED: "이수완료",
};

export default function CourseCard({ course, onClick }: Props) {
  return (
    <div
      onClick={() => onClick(course.courseId)}
      className="rounded-xl"
      style={{
        background: "var(--card)",
        border: "1px solid var(--border)",
        padding: "1rem 1.25rem",
        cursor: "pointer",
        marginBottom: 12,
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <GradeBadge level={course.level} />
          {course.category && (
            <span style={{ fontSize: 12, color: "#94A3B8", fontWeight: 500 }}>{course.category}</span>
          )}
        </div>
        <span style={{ fontSize: 12, color: "#94A3B8", fontWeight: 500 }}>
          {STATUS_LABEL[course.myStatus]}
        </span>
      </div>

      <h3 style={{ margin: "10px 0 4px", fontSize: 16, color: "#0F172A", fontWeight: 600 }}>{course.title}</h3>
      {course.description && (
        <p style={{ margin: "0 0 12px", fontSize: 13, color: "#94A3B8", lineHeight: 1.5 }}>
          {course.description}
        </p>
      )}

      <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: "#94A3B8", marginBottom: 6 }}>
        <span>차시 {course.lessonCount}개{course.hasExam ? " · 필기시험 포함" : ""}</span>
        <span style={{ color: "#CBD5E1", fontWeight: 600 }}>{course.myProgressRate.toFixed(0)}%</span>
      </div>
      <ProgressBar rate={course.myProgressRate} />
    </div>
  );
}
