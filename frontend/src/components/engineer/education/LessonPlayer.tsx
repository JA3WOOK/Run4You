import { useEffect, useRef } from "react";
import type { LessonItem } from "../../../api/education";

interface Props {
  lesson: LessonItem;
  onProgress: (lessonId: number, watchedSeconds: number) => void;
}

// 서버로 진도를 보고하는 최소 간격(초) — 너무 잦은 API 호출 방지
const REPORT_INTERVAL_SEC = 5;

export default function LessonPlayer({ lesson, onProgress }: Props) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const lastReportedRef = useRef<number>(lesson.watchedSeconds);
  const maxWatchedRef = useRef<number>(lesson.watchedSeconds);

  // 차시가 바뀌면 이전 최고 시청 지점부터 이어보기
  useEffect(() => {
    maxWatchedRef.current = lesson.watchedSeconds;
    lastReportedRef.current = lesson.watchedSeconds;
    if (videoRef.current) {
      videoRef.current.currentTime = lesson.watchedSeconds;
    }
  }, [lesson.lessonId]); // eslint-disable-line react-hooks/exhaustive-deps

  const flush = (seconds: number) => {
    const rounded = Math.floor(seconds);
    if (rounded > lastReportedRef.current) {
      lastReportedRef.current = rounded;
      onProgress(lesson.lessonId, rounded);
    }
  };

  const handleTimeUpdate = () => {
    const video = videoRef.current;
    if (!video) return;
    maxWatchedRef.current = Math.max(maxWatchedRef.current, video.currentTime);

    if (maxWatchedRef.current - lastReportedRef.current >= REPORT_INTERVAL_SEC) {
      flush(maxWatchedRef.current);
    }
  };

  const handlePauseOrEnd = () => {
    flush(maxWatchedRef.current);
  };

  useEffect(() => {
    // 언마운트 시 마지막 시청 지점 보고
    return () => flush(maxWatchedRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [lesson.lessonId]);

  return (
    <div>
      <video
        ref={videoRef}
        key={lesson.lessonId}
        src={lesson.videoUrl}
        controls
        style={{ width: "100%", borderRadius: 8, background: "#000" }}
        onTimeUpdate={handleTimeUpdate}
        onPause={handlePauseOrEnd}
        onEnded={handlePauseOrEnd}
      />
      <p style={{ marginTop: 8, fontSize: 13, color: "#94A3B8" }}>
        {lesson.title} · {lesson.progressRate.toFixed(0)}% 시청함
        {lesson.completed && <span style={{ color: "#22C55E", fontWeight: 600 }}> · 완료</span>}
      </p>
    </div>
  );
}
