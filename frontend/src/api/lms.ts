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
  videoUrl: string | null;
  durationSeconds: number;
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

export interface ExamQuestion {
  id: number;
  question: string;
  choices: string[];
  answer: string;             // 정답 보기 번호 (예: "2")
  score: number;
}

export interface Exam {
  id: number;
  courseId: number;
  title: string;
  passScore: number | null;   // Course.passScore
  questions: ExamQuestion[];
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

// 차시 (동영상 교육)
export async function getLessons(token: string, courseId: number): Promise<Lesson[]> {
  const res = await api.get(`/lms/courses/${courseId}/lessons`, { headers: authHeader(token) });
  return res.data.data;
}

export async function createLesson(token: string, courseId: number, data: {
  title: string; content?: string; videoUrl?: string; durationSeconds?: number; orderIndex: number;
}): Promise<Lesson> {
  const res = await api.post(`/lms/courses/${courseId}/lessons`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateLesson(token: string, lessonId: number, data: {
  title: string; content?: string; videoUrl?: string; durationSeconds?: number; orderIndex: number;
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

// 시험 (코스당 1개) / 문항
export async function getExamByCourse(token: string, courseId: number): Promise<Exam | null> {
  const res = await api.get(`/lms/courses/${courseId}/exam`, { headers: authHeader(token) });
  return res.data.data;
}

export async function createExam(token: string, courseId: number, data: {
  title: string;
}): Promise<Exam> {
  const res = await api.post(`/lms/courses/${courseId}/exam`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateExam(token: string, examId: number, data: {
  title: string;
}): Promise<Exam> {
  const res = await api.put(`/lms/exams/${examId}`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function deleteExam(token: string, examId: number): Promise<void> {
  await api.delete(`/lms/exams/${examId}`, { headers: authHeader(token) });
}

export async function addExamQuestion(token: string, examId: number, data: {
  question: string; choices: string[]; answer: string; score: number;
}): Promise<ExamQuestion> {
  const res = await api.post(`/lms/exams/${examId}/questions`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function updateExamQuestion(token: string, questionId: number, data: {
  question: string; choices: string[]; answer: string; score: number;
}): Promise<ExamQuestion> {
  const res = await api.put(`/lms/questions/${questionId}`, data, { headers: authHeader(token) });
  return res.data.data;
}

export async function deleteExamQuestion(token: string, questionId: number): Promise<void> {
  await api.delete(`/lms/questions/${questionId}`, { headers: authHeader(token) });
}