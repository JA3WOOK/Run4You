import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export interface SettlementItem {
  id: number;
  invoiceNumber: string;
  reportId: number;
  brandId: number;
  engineerId: number;
  partsCost: number;
  laborCost: number;
  emergencyFee: number;
  billedAmount: number;
  vatAmount: number;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'FLAGGED';
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | 'PAID';
  flagReason: string | null;
  approvedBy: number | null;
  createdAt: string;
}

export interface SettlementSummary {
  reviewPendingAmount: number;
  approvedAmount: number;
  flaggedCount: number;
}

export interface SettlementListResponse {
  summary: SettlementSummary;
  items: SettlementItem[];
}

export async function getSettlements(token: string): Promise<SettlementListResponse> {
  const res = await api.get('/settlements', { headers: authHeader(token) });
  return res.data.data;
}

export async function approveSettlement(token: string, id: number): Promise<SettlementItem> {
  const res = await api.patch(`/settlements/${id}/approve`, {}, { headers: authHeader(token) });
  return res.data.data;
}

export async function rejectSettlement(token: string, id: number): Promise<SettlementItem> {
  const res = await api.patch(`/settlements/${id}/reject`, {}, { headers: authHeader(token) });
  return res.data.data;
}
