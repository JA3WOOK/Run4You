import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export interface SettlementSummary {
  totalCount: number;
  totalBilled: number;
  pendingAmount: number;
  approvedAmount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
  flaggedCount: number;
}
export interface RepairSummary {
  totalReports: number;
  totalPartsCost: number;
  totalLaborCost: number;
  totalCost: number;
}
export interface GradeDistribution {
  a: number; b: number; c: number; d: number; avgScore: number;
}
export interface CategoryDefect {
  category: string; replacedQuantity: number; occurrences: number;
}
export interface EquipmentMtbf {
  equipmentId: number; failureCount: number; mtbfDays: number | null;
}
export interface EngineerStat {
  engineerId: number; repairCount: number;
}
export interface DashboardResponse {
  settlement: SettlementSummary;
  repair: RepairSummary;
  gradeDistribution: GradeDistribution;
  defectsByCategory: CategoryDefect[];
  mtbf: EquipmentMtbf[];
  overallMtbfDays: number | null;
  engineerStats: EngineerStat[];
}

export async function getDashboard(token: string): Promise<DashboardResponse> {
  const res = await api.get('/dashboard', { headers: authHeader(token) });
  return res.data.data;
}
