import type { CourseLevel } from "../../../api/education";

const LABEL: Record<CourseLevel, string> = {
  BEGINNER: "초급",
  INTERMEDIATE: "중급",
  ADVANCED: "고급",
};

// Sidebar의 역할 뱃지와 동일한 방식(accent + 15% 배경 + 투명 테두리)으로 대비를 확보
const ACCENT: Record<CourseLevel, string> = {
  BEGINNER: "#22C55E",
  INTERMEDIATE: "#8B5CF6",
  ADVANCED: "#F59E0B",
};

export default function GradeBadge({ level }: { level: CourseLevel }) {
  const c = ACCENT[level];
  return (
    <span
      style={{
        display: "inline-block",
        fontSize: 12,
        fontWeight: 700,
        padding: "3px 10px",
        borderRadius: 20,
        background: `${c}22`,
        color: c,
        border: `1px solid ${c}55`,
      }}
    >
      {LABEL[level]}
    </span>
  );
}
