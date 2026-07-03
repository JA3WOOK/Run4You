const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// ─── 타입 정의 ────────────────────────────────────────────────────

export interface MatchingQueueItem {
  rank: number;
  asRequestId: number;
  asRequestNo: string;
  storeName: string;
  storeDistrict: string;
  priority: "EMERGENCY" | "NORMAL";
  errorCode?: string;
  aiCauseDescription?: string;
  aiRecommendedParts?: string;
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
  equipmentId: number;
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

export interface ActiveAssignment {
  assignmentId: number;
  asRequestId: number;
  equipmentId: number;
  status: string;
}

export interface AcceptResult {
    assignmentId: number;
    asRequestId: number;
    equipmentId: number;
    engineerId: number;
    status: string;
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
export async function acceptAssignment(asRequestId: number, token: string | null): Promise<AcceptResult> {
  const res = await fetch(`${BASE_URL}/api/assignments/requests/${asRequestId}/accept`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
  });
  if (!res.ok) {
    let message = "수락 처리 실패";
    try { message = (await res.json()).message ?? message; } catch {}
    throw new Error(message);
  }
  const body = await res.json();
  return body.data; // { assignmentId, asRequestId, equipmentId, engineerId, status }
}

/** 내 활성 배정 조회 — GET /api/assignments/my-active */
export async function fetchMyActiveAssignment(token: string | null): Promise<ActiveAssignment | null> {
  const res = await fetch(`${BASE_URL}/api/assignments/my-active`, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (res.status === 204) return null;
  if (!res.ok) throw new Error("현재 배정 조회 실패");
  const body = await res.json();
  return body.data; // null 또는 { assignmentId, asRequestId, status }
}