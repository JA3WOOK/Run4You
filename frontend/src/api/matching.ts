const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

function authHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${getToken()}`,
  };
}

// ─── 타입 정의 ────────────────────────────────────────────────────

export interface MatchingQueueItem {
  rank: number;
  asRequestId: number;
  asRequestNo: string;
  storeName: string;
  storeDistrict: string;
  priority: "EMERGENCY" | "NORMAL";
  errorCode?: string;
  equipmentType: string;
  equipmentModel: string;
  receivedTime: string;
  distanceKm: number;
  etaMinutes: number;
  totalScore: number;
  distanceScore: number;
  specialtyScore: number;
  ratingScore: number;
  availabilityScore: number;
  urgencyScore: number;
  distanceWeight: number;
  specialtyWeight: number;
  ratingWeight: number;
  availabilityWeight: number;
  urgencyWeight: number;
}

export interface AssignmentDetail {
  asRequestId: number;
  asRequestNo: string;
  storeName: string;
  storeAddress: string;
  priority: "EMERGENCY" | "NORMAL";
  symptom: string;
  errorCode?: string;
  equipmentName: string;
  serialNumber: string;
  purchasedDate: string;
  lastRepairedDate: string;
  equipmentCategory: string;
  totalScore: number;
  distanceScore: number;
  specialtyScore: number;
  ratingScore: number;
  availabilityScore: number;
  urgencyScore: number;
  distanceWeight: number;
  specialtyWeight: number;
  ratingWeight: number;
  availabilityWeight: number;
  urgencyWeight: number;
  distanceKm: number;
  etaMinutes: number;
  trafficCondition: string;
}

// ─── API 호출 ─────────────────────────────────────────────────────

/** 출동 대기열 조회 — GET /api/assignments/queue */
export async function fetchMatchingQueue(token: string | null): Promise<MatchingQueueItem[]> {
  const res = await fetch(`${BASE_URL}/api/assignments/queue`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error("대기열 조회 실패");
  const body = await res.json();
  return body.data;
}

/** 출동 상세 조회 — GET /api/assignments/requests/{id}/detail */
export async function fetchRequestDetail(asRequestId: number, token: string | null): Promise<AssignmentDetail> {
  const res = await fetch(`${BASE_URL}/api/assignments/requests/${asRequestId}/detail`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error("상세 조회 실패");
  const body = await res.json();
  return body.data;
}

/** 수락 — POST /api/assignments/requests/{id}/accept */
export async function acceptAssignment(asRequestId: number, token: string | null): Promise<number> {
  const res = await fetch(`${BASE_URL}/api/assignments/requests/${asRequestId}/accept`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (res.status === 409) throw new Error("이미 다른 엔지니어가 수락했습니다.");
  if (!res.ok) throw new Error("수락 처리 실패");
  const body = await res.json();
  return body.data; // assignmentId
}
