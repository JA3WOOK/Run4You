import { apiClient as api } from './apiClient';

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export interface SuperKpi {
  totalAsRequests: number;
  pendingApprovals: number;
  activeEngineers: number;
  thisMonthCommission: number;
}

export interface PendingBrandItem {
  id: number;
  name: string;
  businessNo: string;
  commissionRate: number;
}

export interface PendingUserItem {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface BrandStat {
  id: number;
  name: string;
  status: string;
  commissionRate: number;
  totalAsCount: number;
  completedAsCount: number;
  totalBilled: number;
  avgProcessingHours: number | null;
}

export interface CategoryStat {
  category: string;
  count: number;
}

export interface MonthlyCommission {
  month: string;
  amount: number;
}

export interface SuperDashboardData {
  kpi: SuperKpi;
  pendingBrands: PendingBrandItem[];
  pendingUsers: PendingUserItem[];
  brandStats: BrandStat[];
  categoryStats: CategoryStat[];
  monthlyCommission: MonthlyCommission[];
}

export async function getSuperDashboard(token: string): Promise<SuperDashboardData> {
  const res = await api.get('/dashboard/super', { headers: authHeader(token) });
  return res.data.data;
}
