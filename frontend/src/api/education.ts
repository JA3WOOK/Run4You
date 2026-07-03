// src/api/education.ts
// 엔지니어 교육(LMS) API — 코스 목록/상세, 차시 진도, 시험, 매뉴얼
// ※ v5: 이 앱의 다른 api 파일들(dispatch.ts, notification.ts)과 동일하게
//   accessToken을 localStorage에서 읽지 않고 함수 인자로 명시적으로 받는다.

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

function authHeaders(accessToken: string) {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${accessToken}`,
  };
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

async function handle<T>(res: Response): Promise<T> {
  const body: ApiResponse<T> = await res.json();
  if (!res.ok || !body.success) {
    throw new Error(body.message ?? `요청 실패 (status ${res.status})`);
  }
  return body.data;
}

// ─── 타입 정의 ────────────────────────────────────────────────────

// 코스 등급(lms.entity.CourseLevel)과 엔지니어 기술 등급(SkillGrade)은 값이 동일하다.
export type CourseLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type SkillGrade = CourseLevel;

export type EnrollmentStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
export type ManualType = "DISPATCH_GUIDE" | "SYMPTOM_GUIDE";

export interface CourseListItem {
  courseId: number;
  title: string;
  gradeLabel: string;
  level: CourseLevel;
  category: string | null;
  description: string | null;
  lessonCount: number;
  myProgressRate: number;
  myStatus: EnrollmentStatus;
  hasExam: boolean;
}

export interface LessonItem {
  lessonId: number;
  title: string;
  videoUrl: string;
  durationSeconds: number;
  sortOrder: number;
  content: string | null;
  watchedSeconds: number;
  progressRate: number;
  completed: boolean;
}

export interface CourseDetail {
  courseId: number;
  title: string;
  gradeLabel: string;
  level: CourseLevel;
  category: string | null;
  description: string | null;
  myProgressRate: number;
  myStatus: EnrollmentStatus;
  hasExam: boolean;
  lessons: LessonItem[];
}

export interface LessonProgressResult {
  lessonId: number;
  lessonProgressRate: number;
  lessonCompleted: boolean;
  courseProgressRate: number;
  courseStatus: EnrollmentStatus;
}

export interface ExamQuestion {
  questionId: number;
  question: string;
  choices: string[];
  score: number;
}

export interface Exam {
  examId: number;
  courseId: number;
  title: string;
  passScore: number;
  totalScore: number;
  questions: ExamQuestion[];
}

export interface ExamResult {
  attemptId: number;
  score: number;
  passScore: number;
  passed: boolean;
  gradeUpgraded: boolean;
  newGrade: SkillGrade | null;
}

export interface Manual {
  manualId: number;
  manualType: ManualType;
  title: string;
  faultCategory: string | null;
  content: string;
}

// ─── API 호출 (모두 accessToken을 첫 인자로 받음) ──────────────────

export async function fetchCourses(accessToken: string, level?: CourseLevel): Promise<CourseListItem[]> {
  const qs = level ? `?level=${level}` : "";
  const res = await fetch(`${BASE_URL}/api/engineer/courses${qs}`, {
    headers: authHeaders(accessToken),
  });
  return handle<CourseListItem[]>(res);
}

export async function fetchCourseDetail(accessToken: string, courseId: number): Promise<CourseDetail> {
  const res = await fetch(`${BASE_URL}/api/engineer/courses/${courseId}`, {
    headers: authHeaders(accessToken),
  });
  return handle<CourseDetail>(res);
}

export async function updateLessonProgress(
  accessToken: string,
  lessonId: number,
  watchedSeconds: number
): Promise<LessonProgressResult> {
  const res = await fetch(`${BASE_URL}/api/engineer/lessons/${lessonId}/progress`, {
    method: "POST",
    headers: authHeaders(accessToken),
    body: JSON.stringify({ watchedSeconds }),
  });
  return handle<LessonProgressResult>(res);
}

export async function fetchExam(accessToken: string, courseId: number): Promise<Exam> {
  const res = await fetch(`${BASE_URL}/api/engineer/courses/${courseId}/exam`, {
    headers: authHeaders(accessToken),
  });
  return handle<Exam>(res);
}

export async function submitExam(
  accessToken: string,
  examId: number,
  answers: { questionId: number; answer: string }[]
): Promise<ExamResult> {
  const res = await fetch(`${BASE_URL}/api/engineer/exams/${examId}/submit`, {
    method: "POST",
    headers: authHeaders(accessToken),
    body: JSON.stringify({ answers }),
  });
  return handle<ExamResult>(res);
}

export async function fetchManuals(
  accessToken: string,
  manualType?: ManualType,
  faultCategory?: string
): Promise<Manual[]> {
  const params = new URLSearchParams();
  if (manualType) params.set("manualType", manualType);
  if (faultCategory) params.set("faultCategory", faultCategory);
  const qs = params.toString() ? `?${params.toString()}` : "";
  const res = await fetch(`${BASE_URL}/api/engineer/manuals${qs}`, {
    headers: authHeaders(accessToken),
  });
  return handle<Manual[]>(res);
}
