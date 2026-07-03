import axios from 'axios';
import type { EquipmentCategory } from './equipment';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}

export type Priority = 'EMERGENCY' | 'NORMAL';
export type AsStatus = 'RECEIVED' | 'MATCHING' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

// A/S 접수 입력 (AsRequestCreateDto)
export interface AsRequestCreateRequest {
    equipmentId: number;
    priority: Priority;
    errorCode?: string;
    symptom: string;
}

// A/S 접수 완료 응답 (AsRequestResponseDto)
export interface AsRequestResponse {
    id: number;
    priority: Priority;
    status: AsStatus;
    requestedAt: string;
    equipmentId: number;
    equipmentName: string;
    errorCode: string | null;
    symptom: string | null;
}

// A/S 접수 생성 (POST /api/as-requests)
export async function createAsRequest(
    token: string,
    data: AsRequestCreateRequest
): Promise<AsRequestResponse> {
    const res = await api.post('/as-requests', data, { headers: authHeader(token) });
    return res.data.data;
}

// 고장 기자재의 진행 중인 A/S 접수 상세 조회 (GET /api/as-requests/by-equipment/{equipmentId})
export async function getActiveAsRequestByEquipment(
    token: string,
    equipmentId: number
): Promise<AsRequestResponse> {
    const res = await api.get(`/as-requests/by-equipment/${equipmentId}`, { headers: authHeader(token) });
    return res.data.data;
}

// 진행 단계 (DispatchStatus name)
export type DispatchStatusName =
    | 'PENDING_ACCEPT' | 'ACCEPTED' | 'DISPATCHED'
    | 'ARRIVED' | 'REPAIRING' | 'COMPLETED' | 'CANCELLED';

// 진행 중 A/S 한 건 (InProgressItemDto)
export interface InProgressAsItem {
    asRequestId: number;
    requestNo: string;
    requestedAt: string;
    equipmentId: number;
    equipmentName: string;
    modelName: string;
    category: EquipmentCategory;
    currentStatus: DispatchStatusName | null;
    assignmentId: number | null;
    engineerName: string | null;
    engineerPhone: string | null;
    etaMinutes: number | null;
}

// 진행 중 A/S 목록 응답 (InProgressAsListResponseDto)
export interface InProgressAsListResponse {
    requests: InProgressAsItem[];
    totalCount: number;
}

// 진행 중인 A/S 목록 조회 (GET /api/as-requests/in-progress)
export async function getInProgressAsList(
    token: string
): Promise<InProgressAsListResponse> {
    const res = await api.get('/as-requests/in-progress', { headers: authHeader(token) });
    return res.data.data;
}

// A/S 접수 취소 (PATCH /api/as-requests/{asRequestId}/cancel, 배정 전 상태에서만 가능)
export async function cancelAsRequest(
    token: string,
    asRequestId: number
): Promise<string> {
    const res = await api.patch(`/as-requests/${asRequestId}/cancel`, null, { headers: authHeader(token) });
    return res.data.data;
}