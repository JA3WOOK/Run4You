// src/components/engineer/education/LessonPlayer.tsx
import { useEffect, useRef, useState } from "react";
import type { LessonItem } from "../../../api/education";

interface Props {
  lesson: LessonItem;
  onProgress: (lessonId: number, watchedSeconds: number) => void;
}

// 서버로 진도를 보고하는 최소 간격(초) — 너무 잦은 API 호출 방지
const REPORT_INTERVAL_SEC = 5;

/** youtube.com/watch?v=ID, youtu.be/ID, youtube.com/embed/ID 등에서 영상 ID 추출 */
function extractYouTubeId(url: string): string | null {
  try {
    const u = new URL(url);
    const host = u.hostname.replace(/^www\./, "");
    if (host === "youtu.be") {
      return u.pathname.slice(1).split("/")[0] || null;
    }
    if (host === "youtube.com" || host === "m.youtube.com" || host === "music.youtube.com") {
      if (u.pathname === "/watch") return u.searchParams.get("v");
      if (u.pathname.startsWith("/embed/")) return u.pathname.split("/embed/")[1]?.split("/")[0] ?? null;
      if (u.pathname.startsWith("/shorts/")) return u.pathname.split("/shorts/")[1]?.split("/")[0] ?? null;
    }
    return null;
  } catch {
    return null;
  }
}

export default function LessonPlayer({ lesson, onProgress }: Props) {
  const youtubeId = extractYouTubeId(lesson.videoUrl);

  return (
    <div>
      {youtubeId ? (
        <YouTubePlayer
          key={lesson.lessonId}
          videoId={youtubeId}
          lessonId={lesson.lessonId}
          startSeconds={lesson.watchedSeconds}
          onProgress={onProgress}
        />
      ) : (
        <NativeVideoPlayer key={lesson.lessonId} lesson={lesson} onProgress={onProgress} />
      )}
      <p style={{ marginTop: 8, fontSize: 13, color: "#94A3B8" }}>
        {lesson.title} · {lesson.progressRate.toFixed(0)}% 시청함
        {lesson.completed && <span style={{ color: "#22C55E", fontWeight: 600 }}> · 완료</span>}
      </p>
      {lesson.content && (
        <div
          style={{
            marginTop: 12,
            padding: "14px 16px",
            borderRadius: 8,
            background: "var(--card)",
            border: "1px solid var(--border)",
            fontSize: 13,
            color: "#0f172a",
            lineHeight: 1.7,
            whiteSpace: "pre-line",
          }}
        >
          {lesson.content}
        </div>
      )}
    </div>
  );
}

// ── 일반 동영상 파일(mp4 등) ──────────────────────────────────────

function NativeVideoPlayer({ lesson, onProgress }: { lesson: LessonItem; onProgress: Props["onProgress"] }) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const lastReportedRef = useRef<number>(lesson.watchedSeconds);
  const maxWatchedRef = useRef<number>(lesson.watchedSeconds);

  useEffect(() => {
    if (videoRef.current) {
      videoRef.current.currentTime = lesson.watchedSeconds;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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

  const handlePauseOrEnd = () => flush(maxWatchedRef.current);

  useEffect(() => {
    return () => flush(maxWatchedRef.current);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <video
      ref={videoRef}
      src={lesson.videoUrl}
      controls
      style={{ width: "100%", borderRadius: 8, background: "#000" }}
      onTimeUpdate={handleTimeUpdate}
      onPause={handlePauseOrEnd}
      onEnded={handlePauseOrEnd}
    />
  );
}

// ── 유튜브 (IFrame Player API) ────────────────────────────────────

declare global {
  interface Window {
    YT?: {
      Player: new (elementId: string, options: Record<string, unknown>) => YouTubePlayerInstance;
      PlayerState: { PLAYING: number; PAUSED: number; ENDED: number };
    };
    onYouTubeIframeAPIReady?: () => void;
  }
}

interface YouTubePlayerInstance {
  getCurrentTime: () => number;
  destroy: () => void;
}

let youTubeApiPromise: Promise<void> | null = null;

function loadYouTubeApi(): Promise<void> {
  if (window.YT?.Player) return Promise.resolve();
  if (youTubeApiPromise) return youTubeApiPromise;

  youTubeApiPromise = new Promise((resolve) => {
    const previous = window.onYouTubeIframeAPIReady;
    window.onYouTubeIframeAPIReady = () => {
      previous?.();
      resolve();
    };
    if (!document.querySelector('script[src="https://www.youtube.com/iframe_api"]')) {
      const script = document.createElement("script");
      script.src = "https://www.youtube.com/iframe_api";
      document.head.appendChild(script);
    }
  });
  return youTubeApiPromise;
}

function YouTubePlayer({
  videoId,
  lessonId,
  startSeconds,
  onProgress,
}: {
  videoId: string;
  lessonId: number;
  startSeconds: number;
  onProgress: Props["onProgress"];
}) {
  const containerId = `yt-player-${lessonId}`;
  const playerRef = useRef<YouTubePlayerInstance | null>(null);
  const lastReportedRef = useRef<number>(startSeconds);
  const pollRef = useRef<number | null>(null);
  const [embedBlocked, setEmbedBlocked] = useState(false);

  useEffect(() => {
    let destroyed = false;
    setEmbedBlocked(false);

    loadYouTubeApi().then(() => {
      if (destroyed || !window.YT) return;
      playerRef.current = new window.YT.Player(containerId, {
        videoId,
        playerVars: { start: Math.floor(startSeconds), rel: 0 },
        events: {
          onReady: () => {
            pollRef.current = window.setInterval(() => {
              const player = playerRef.current;
              if (!player) return;
              const current = player.getCurrentTime();
              if (current - lastReportedRef.current >= REPORT_INTERVAL_SEC) {
                lastReportedRef.current = Math.floor(current);
                onProgress(lessonId, Math.floor(current));
              }
            }, 1000);
          },
          onError: (e: { data: number }) => {
            // 101, 150 = 영상 소유자가 임베드(다른 사이트 재생)를 막아둔 경우
            if (e.data === 101 || e.data === 150) {
              setEmbedBlocked(true);
              if (pollRef.current) window.clearInterval(pollRef.current);
            }
          },
          onStateChange: (e: { data: number }) => {
            const ended = window.YT?.PlayerState.ENDED === e.data;
            const paused = window.YT?.PlayerState.PAUSED === e.data;
            if ((ended || paused) && playerRef.current) {
              const current = playerRef.current.getCurrentTime();
              if (Math.floor(current) > lastReportedRef.current) {
                lastReportedRef.current = Math.floor(current);
                onProgress(lessonId, Math.floor(current));
              }
            }
          },
        },
      });
    });

    return () => {
      destroyed = true;
      if (pollRef.current) window.clearInterval(pollRef.current);
      if (playerRef.current) {
        try {
          const current = playerRef.current.getCurrentTime();
          if (Math.floor(current) > lastReportedRef.current) {
            onProgress(lessonId, Math.floor(current));
          }
        } catch {
          // 임베드가 막힌 상태에서는 getCurrentTime이 실패할 수 있음 — 무시
        }
        playerRef.current.destroy();
        playerRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [videoId]);

  if (embedBlocked) {
    return (
      <div
        style={{
          width: "100%",
          aspectRatio: "16 / 9",
          borderRadius: 8,
          background: "var(--card)",
          border: "1px solid var(--border)",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: 10,
          padding: 24,
          textAlign: "center",
        }}
      >
        <p style={{ fontSize: 14, color: "#0F172A", fontWeight: 600, margin: 0 }}>
          이 영상은 다른 사이트에서 재생할 수 없도록 설정되어 있어요
        </p>
        <p style={{ fontSize: 12, color: "#94A3B8", margin: 0 }}>
          영상 소유자가 임베드(퍼가기)를 막아둔 영상이라 여기서는 재생할 수 없습니다.
          아래 링크로 유튜브에서 직접 시청해주세요.
        </p>
        <a
          href={`https://www.youtube.com/watch?v=${videoId}`}
          target="_blank"
          rel="noreferrer"
          style={{
            marginTop: 4,
            padding: "8px 16px",
            borderRadius: 8,
            background: "var(--primary)",
            color: "#fff",
            fontSize: 13,
            fontWeight: 600,
            textDecoration: "none",
          }}
        >
          유튜브에서 보기
        </a>
      </div>
    );
  }

  return (
    <div style={{ position: "relative", width: "100%", paddingTop: "56.25%", borderRadius: 8, overflow: "hidden", background: "#000" }}>
      <div id={containerId} style={{ position: "absolute", inset: 0 }} />
    </div>
  );
}
