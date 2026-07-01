import axios from 'axios';
import type { DispatchStatus } from './dispatch';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}

// 관제 대시보드 "실시간 출동 현황" 1건 (ActiveDispatchResponse)
export interface ActiveDispatch {
    assignmentId: number;
    asRequestId: number;
    status: DispatchStatus;
    storeName: string;
    engineerName: string | null;
    engineerPhone: string | null;
    equipmentName: string | null;
    equipmentCategory: string | null;
    priority: 'EMERGENCY' | 'NORMAL' | string;
    etaMinutes: number | null;
    latitude: number | null;
    longitude: number | null;
    changedAt: string | null;
}

// GET /api/admin/dispatches/active — 브랜드(총괄=전체) 진행 중 출동
// ⚠ Domain④ 컨벤션: ApiResponse 래핑 없음 → res.data 직접 사용
export async function getActiveDispatches(token: string): Promise<ActiveDispatch[]> {
    const res = await api.get('/admin/dispatches/active', { headers: authHeader(token) });
    return res.data;
}
