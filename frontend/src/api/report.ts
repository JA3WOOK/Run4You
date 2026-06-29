import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

// ===== 타입 =====
export interface Part {
  id: number;
  partCode: string;
  name: string;
  category: string | null;
  unitPrice: number;
}

export interface PartLine {
  partCode: string;
  quantity: number;
  appliedPrice: number;
}

export interface ReportCreateRequest {
  assignmentId: number;
  asRequestId: number;
  engineerId: number;
  equipmentId?: number | null;
  laborCost: number;
  diagnosis?: string;
  parts: PartLine[];
}

export interface PartLineResponse {
  partId: number;
  partCode: string;
  partName: string;
  quantity: number;
  appliedPrice: number;
  standardPrice: number;
  priceMatched: boolean;
  lineTotal: number;
}

export interface ReportResponse {
  id: number;
  assignmentId: number;
  asRequestId: number;
  engineerId: number;
  equipmentId: number | null;
  laborCost: number;
  partsCost: number;
  totalCost: number;
  diagnosis: string | null;
  hasPriceMismatch: boolean;
  mismatchMessages: string[];
  parts: PartLineResponse[];
  createdAt: string;
}

// ===== API =====
/** 부품 마스터 목록 (정비 리포트 작성 시 부품 선택용) */
export async function getParts(token: string): Promise<Part[]> {
  const res = await api.get('/parts', { headers: authHeader(token) });
  return res.data.data;
}

/** 정비 리포트 작성 (부품 단가 검증 + 비용 합산은 서버에서 처리) */
export async function createReport(token: string, body: ReportCreateRequest): Promise<ReportResponse> {
  const res = await api.post('/reports', body, { headers: authHeader(token) });
  return res.data.data;
}
