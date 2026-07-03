import { useState, useEffect } from "react";
import { useCourseDetail } from "../../hooks/useCourseDetail";
import LessonList from "../../components/engineer/education/LessonList";
import LessonPlayer from "../../components/engineer/education/LessonPlayer";
import ProgressBar from "../../components/engineer/education/ProgressBar";
import GradeBadge from "../../components/engineer/education/GradeBadge";

interface Props {
  accessToken: string;
  courseId: number;
  onBack: () => void;
  onOpenExam: (courseId: number) => void;
}

export default function EngineerCourseDetailPage({ accessToken, courseId, onBack, onOpenExam }: Props) {
  const { course, loading, error, reportProgress } = useCourseDetail(accessToken, courseId);
  const [activeLessonId, setActiveLessonId] = useState<number | null>(null);

  useEffect(() => {
    if (course && activeLessonId === null && course.lessons.length > 0) {
      const next = course.lessons.find((l) => !l.completed) ?? course.lessons[0];
      setActiveLessonId(next.lessonId);
    }
  }, [course, activeLessonId]);

  if (loading) return <p style={{ color: "#94A3B8" }}>불러오는 중...</p>;
  if (error) return <p style={{ color: "#F87171" }}>{error}</p>;
  if (!course) return null;

  const activeLesson = course.lessons.find((l) => l.lessonId === activeLessonId) ?? course.lessons[0];
  const gateReady = course.myProgressRate >= 100;

  return (
    <div>
      <button
        onClick={onBack}
        style={{ background: "none", border: "none", color: "#94A3B8", cursor: "pointer", marginBottom: 12, fontSize: 13 }}
      >
        ← 코스 목록으로
      </button>

      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
        <GradeBadge level={course.level} />
        <h1 style={{ fontSize: 18, color: "#0F172A", margin: 0, fontWeight: 700 }}>{course.title}</h1>
      </div>
      {course.description && (
        <p style={{ fontSize: 13, color: "#94A3B8", margin: "6px 0 16px" }}>{course.description}</p>
      )}

      <div style={{ marginBottom: 20 }}>
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: "#94A3B8", marginBottom: 6 }}>
          <span>코스 진도율</span>
          <span style={{ color: "#CBD5E1", fontWeight: 600 }}>{course.myProgressRate.toFixed(0)}%</span>
        </div>
        <ProgressBar rate={course.myProgressRate} height={10} />
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 24 }}>
        <div>
          {activeLesson && (
            <LessonPlayer
              lesson={activeLesson}
              onProgress={(lessonId, seconds) => reportProgress(lessonId, seconds)}
            />
          )}
        </div>

        <div>
          <p style={{ fontSize: 12, color: "#94A3B8", marginBottom: 8, fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.04em" }}>
            차시 목록
          </p>
          <LessonList
            lessons={course.lessons}
            activeLessonId={activeLesson?.lessonId ?? -1}
            onSelect={setActiveLessonId}
          />

          {course.hasExam && (
            <button
              disabled={!gateReady}
              onClick={() => onOpenExam(course.courseId)}
              style={{
                marginTop: 20,
                width: "100%",
                padding: "12px 0",
                borderRadius: 8,
                border: "none",
                background: gateReady ? "#22C55E" : "rgba(255,255,255,0.08)",
                color: gateReady ? "#06210F" : "#64748B",
                cursor: gateReady ? "pointer" : "not-allowed",
                fontWeight: 700,
              }}
            >
              {gateReady ? "필기시험 응시하기" : "진도율 100% 달성 시 시험 응시 가능"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
