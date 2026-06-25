import { useEffect, useState, useCallback } from "react";
import { fetchMatchingQueue } from "../api/matching";
import type { MatchingQueueItem } from "../api/matching";
import { useAuth } from "../context/AuthContext";

export function useMatchingQueue() {
  console.log("useMatchingQueue 실행됨");
  const { accessToken } = useAuth();
  const [queue, setQueue] = useState<MatchingQueueItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    console.log("전달되는 토큰:", accessToken);
    try {
      setLoading(true);
      setError(null);
      const data = await fetchMatchingQueue(accessToken);
      setQueue(data);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    // 30초마다 자동 갱신
    const interval = setInterval(load, 30_000);
    return () => clearInterval(interval);
  }, [load]);

  return { queue, loading, error, refresh: load };
}
