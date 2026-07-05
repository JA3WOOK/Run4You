// src/hooks/useManuals.ts
import { useEffect, useState } from "react";
import { fetchManuals } from "../api/education";
import type { Manual, ManualType } from "../api/education";

export function useManuals(accessToken: string, type?: ManualType, faultCategory?: string) {
  const [manuals, setManuals] = useState<Manual[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!accessToken) return;
    setLoading(true);
    setError(null);
    fetchManuals(accessToken, type, faultCategory)
      .then(setManuals)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [accessToken, type, faultCategory]);

  return { manuals, loading, error };
}
