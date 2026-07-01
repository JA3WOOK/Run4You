import { apiClient as api } from './apiClient';
import type { DispatchStatus } from './dispatch';

function authHeader(token: string) {
    return { Authorization: `Bearer ${token}` };
}

export interface Brand {
    id: number;
    name: string;
    businessNo: string;
    commissionRate: number;
    status: 'PENDING' | 'ACTIVE' | 'INACTIVE';
}

export interface User {
    id: number;
    email: string;
    name: string;
    phone: string;
    role: string;
    status: 'PENDING' | 'ACTIVE' | 'INACTIVE';
    brandId: number | null;
    brandName: string | null;
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

export async function getBrands(token: string): Promise<Brand[]> {
    const res = await api.get('/brands', { headers: authHeader(token) });
    return res.data.data;
}

export async function approveBrand(token: string, id: number): Promise<Brand> {
    const res = await api.patch(`/brands/${id}/approve`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function rejectBrand(token: string, id: number): Promise<Brand> {
    const res = await api.patch(`/brands/${id}/reject`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function updateCommissionRate(token: string, id: number, commissionRate: number): Promise<Brand> {
    const res = await api.patch(`/brands/${id}/commission-rate`, { commissionRate }, { headers: authHeader(token) });
    return res.data.data;
}

export async function deleteBrand(token: string, id: number): Promise<void> {
    await api.delete(`/brands/${id}`, { headers: authHeader(token) });
}

export async function getUsers(token: string): Promise<User[]> {
    const res = await api.get('/users', { headers: authHeader(token) });
    return res.data.data;
}

export async function getPendingUsers(token: string): Promise<User[]> {
    const res = await api.get('/users/pending', { headers: authHeader(token) });
    return res.data.data;
}

export async function approveUser(token: string, id: number): Promise<User> {
    const res = await api.patch(`/users/${id}/approve`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function rejectUser(token: string, id: number): Promise<User> {
    const res = await api.patch(`/users/${id}/reject`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function deactivateUser(token: string, id: number): Promise<User> {
    const res = await api.patch(`/users/${id}/deactivate`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function activateUser(token: string, id: number): Promise<User> {
    const res = await api.patch(`/users/${id}/activate`, {}, { headers: authHeader(token) });
    return res.data.data;
}

export async function deleteUser(token: string, id: number): Promise<void> {
    await api.delete(`/users/${id}`, { headers: authHeader(token) });
}