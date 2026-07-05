import { apiClient as api } from './apiClient';

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export interface TrainingSummary {
  totalEngineers: number;
  totalEnrolled: number;
  totalCompleted: number;
  completionRate: number;
  totalAttempted: number;
  totalPassed: number;
  passRate: number;
}

export interface EngineerTrainingRow {
  engineerId: number;
  engineerName: string;
  skillGrade: string;
  enrolledCount: number;
  completedCount: number;
  completionRate: number;
  attemptedCount: number;
  passedCount: number;
  avgProgressRate: number;
  lastAttemptedAt: string | null;
}

export interface TrainingStatus {
  summary: TrainingSummary;
  engineers: EngineerTrainingRow[];
}

// GET /api/lms/admin/training-status — ApiResponse 래핑이므로 res.data.data
export async function getTrainingStatus(token: string): Promise<TrainingStatus> {
  const res = await api.get('/lms/admin/training-status', { headers: authHeader(token) });
  return res.data.data;
}
