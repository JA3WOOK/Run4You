import { apiClient as api } from './apiClient';

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
