// src/components/engineer/education/LessonPlayer.tsx
import { useEffect, useRef } from "react";
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

  useEffect(() => {
    let destroyed = false;

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
        const current = playerRef.current.getCurrentTime();
        if (Math.floor(current) > lastReportedRef.current) {
          onProgress(lessonId, Math.floor(current));
        }
        playerRef.current.destroy();
        playerRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [videoId]);

  return (
    <div style={{ position: "relative", width: "100%", paddingTop: "56.25%", borderRadius: 8, overflow: "hidden", background: "#000" }}>
      <div id={containerId} style={{ position: "absolute", inset: 0 }} />
    </div>
  );
}
