import { apiClient as api } from './apiClient';

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export type CourseLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type ManualType = 'DISPATCH_GUIDE' | 'SYMPTOM_GUIDE' | 'GENERAL';

export interface Course {
  id: number;
  title: string;
  description: string | null;
  level: CourseLevel;
  targetSpecialty: string | null;
  passScore: number | null;
  lessonCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Lesson {
  id: number;
  courseId: number;
  title: string;
  content: string | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface Manual {
  id: number;
  title: string;
  content: string;
  manualType: ManualType;
  faultCategory: string | null;
  createdAt: string;
  updatedAt: string;
}

// 코스
export async function getCourses(token: string): Promise<Course[]> {
  const res = await api.get('/lms/courses', { headers: authHeader(token) });
  return res.data.data;
}

export async function createCourse(token: string, data: {
  title: string; description?: string; level: CourseLevel;
  targetSpecialty?: string; passScore?: number;
}): Promise<Course> {
  const res = await api.post('/lms/courses', data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateCourse(token: string, id: number, data: {
  title: string; description?: string; level: CourseLevel;
  targetSpecialty?: string; passScore?: number;
}): Promise<Course> {
  const res = await api.put(`/lms/courses/${id}`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function deleteCourse(token: string, id: number): Promise<void> {
  await api.delete(`/lms/courses/${id}`, { headers: authHeader(token) });
}

// 차시
export async function getLessons(token: string, courseId: number): Promise<Lesson[]> {
  const res = await api.get(`/lms/courses/${courseId}/lessons`, { headers: authHeader(token) });
  return res.data.data;
}

export async function createLesson(token: string, courseId: number, data: {
  title: string; content?: string; orderIndex: number;
}): Promise<Lesson> {
  const res = await api.post(`/lms/courses/${courseId}/lessons`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateLesson(token: string, lessonId: number, data: {
  title: string; content?: string; orderIndex: number;
}): Promise<Lesson> {
  const res = await api.put(`/lms/lessons/${lessonId}`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function deleteLesson(token: string, lessonId: number): Promise<void> {
  await api.delete(`/lms/lessons/${lessonId}`, { headers: authHeader(token) });
}

// 매뉴얼
export async function getManuals(token: string): Promise<Manual[]> {
  const res = await api.get('/lms/manuals', { headers: authHeader(token) });
  return res.data.data;
}

export async function createManual(token: string, data: {
  title: string; content: string; manualType: ManualType; faultCategory?: string;
}): Promise<Manual> {
  const res = await api.post('/lms/manuals', data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateManual(token: string, id: number, data: {
  title: string; content: string; manualType: ManualType; faultCategory?: string;
}): Promise<Manual> {
  const res = await api.put(`/lms/manuals/${id}`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function deleteManual(token: string, id: number): Promise<void> {
  await api.delete(`/lms/manuals/${id}`, { headers: authHeader(token) });
}
