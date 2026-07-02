import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}
export type EquipmentCategory = 'KIOSK' | 'ESPRESSO' | 'ICE_MAKER' | 'REFRIGERATOR';

// 기자재 카드 1개 (EquipmentResponseDto)
export interface Equipment {
    id: number;
    category: EquipmentCategory;
    name: string;
    modelName: string;
    serialNo: string;
    status: 'OPERATIONAL' | 'FAULTY' | 'REPAIRING';
    errorCode: string | null;
    purchasedAt: string | null;
    nextInspectionDate: string | null;
}

// 목록 + 카운트 (EquipmentListResponseDto)
export interface EquipmentListResponse {
    storeName: string;
    totalCount: number;
    operationalCount: number;
    faultyCount: number;
    repairingCount: number;
    equipments: Equipment[];
}

// 기자재 등록 폼 (EquipmentCreateDto)
export interface EquipmentCreateRequest {
    name: string;
    category: EquipmentCategory;
    modelName: string;
    manufacturer: string;
    serialNo: string;
    purchasedAt?: string;
}

// 검색 필터 (EquipmentSearchDto)
export interface EquipmentSearchParams {
    category?: string;
    keyword?: string;
}

// 수리 이력 1건 (RepairHistoryItem)
export interface RepairHistoryItem {
    completedAt: string | null;
    errorCode: string | null;
    symptom: string | null;
    totalCost: number | null;
    status: 'RECEIVED' | 'MATCHING' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
    diagnosis: string | null;
    repairReportId: number | null;
    asRequestId: number | null;
}

// 이력 모달 전체 (AsRequestHistoryDto)
export interface RepairHistory {
    // 상단 기자재 정보
    name: string;
    modelName: string;
    serialNo: string;
    purchasedAt: string | null;
    lastRepairAt: string | null;
    storeName: string;
    status: 'OPERATIONAL' | 'FAULTY' | 'REPAIRING';

    // 수리 이력 목록
    repairHistoryItems: RepairHistoryItem[];

    // 하단 요약
    totalRepairCount: number;
    totalRepairCost: number | null;
}

// 1. 기자재 목록 조회 (GET /api/equipment)
export async function getEquipmentList(
    token: string,
    params?: EquipmentSearchParams
): Promise<EquipmentListResponse> {
    const res = await api.get('/equipment', { headers: authHeader(token), params });
    return res.data.data;
}

// 2. 기자재 등록 (POST /api/equipment)
export async function registerEquipment(
    token: string,
    data: EquipmentCreateRequest
): Promise<Equipment> {
    const res = await api.post('/equipment', data, { headers: authHeader(token) });
    return res.data.data;
}

// 3. 수리 이력 조회 (GET /api/equipment/{id}/history)
export async function getRepairHistory(
    token: string,
    equipmentId: number
): Promise<RepairHistory> {
    const res = await api.get(`/equipment/${equipmentId}/history`, { headers: authHeader(token) });
    return res.data.data;
}