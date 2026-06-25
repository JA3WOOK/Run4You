import { useEffect, useState } from "react";
import { fetchRequestDetail, acceptAssignment } from "../api/matching";
import type { AssignmentDetail } from "../api/matching";
import { useAuth } from "../context/AuthContext";

export function useAssignmentDetail(asRequestId: number | null) {
  const { accessToken } = useAuth();
  const [detail, setDetail] = useState<AssignmentDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [accepting, setAccepting] = useState(false);

  useEffect(() => {
    if (!asRequestId) return;
    setLoading(true);
    setError(null);
    fetchRequestDetail(asRequestId, accessToken)
      .then(setDetail)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [asRequestId, accessToken]);

  const accept = async (): Promise<number> => {
    if (!asRequestId) throw new Error("요청 ID 없음");
    setAccepting(true);
    try {
      return await acceptAssignment(asRequestId, accessToken);
    } finally {
      setAccepting(false);
    }
  };

  return { detail, loading, error, accept, accepting };
}
