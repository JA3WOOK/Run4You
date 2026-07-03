import type { LessonItem } from "../../../api/education";

interface Props {
  lessons: LessonItem[];
  activeLessonId: number;
  onSelect: (lessonId: number) => void;
}

export default function LessonList({ lessons, activeLessonId, onSelect }: Props) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      {lessons.map((l, idx) => {
        const active = l.lessonId === activeLessonId;
        return (
          <div
            key={l.lessonId}
            onClick={() => onSelect(l.lessonId)}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              padding: "10px 12px",
              borderRadius: 8,
              cursor: "pointer",
              background: active ? "rgba(37,99,235,0.15)" : "transparent",
              borderLeft: active ? "2px solid var(--primary)" : "2px solid transparent",
            }}
          >
            <span
              style={{
                width: 20,
                height: 20,
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 11,
                fontWeight: 700,
                flexShrink: 0,
                background: l.completed ? "#22C55E" : "rgba(255,255,255,0.08)",
                color: l.completed ? "#06210F" : "#94A3B8",
              }}
            >
              {l.completed ? "✓" : idx + 1}
            </span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <p
                style={{
                  margin: 0,
                  fontSize: 13,
                  color: active ? "#0F172A" : "#CBD5E1",
                  fontWeight: active ? 600 : 400,
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  whiteSpace: "nowrap",
                }}
              >
                {l.title}
              </p>
              <p style={{ margin: "2px 0 0", fontSize: 11, color: "#64748B" }}>
                {l.progressRate.toFixed(0)}%
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
