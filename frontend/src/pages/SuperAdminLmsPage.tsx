import { useEffect, useState } from 'react';
import { BookOpen, FileText, Plus, Pencil, Trash2, ChevronDown, ChevronRight, X, Check } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  getCourses, createCourse, updateCourse, deleteCourse,
  getLessons, createLesson, updateLesson, deleteLesson,
  getManuals, createManual, updateManual, deleteManual,
  type Course, type Lesson, type Manual, type CourseLevel, type ManualType,
} from '../api/lms';

const levelLabel: Record<CourseLevel, string> = {
  BEGINNER: '초급', INTERMEDIATE: '중급', ADVANCED: '고급',
};
const manualTypeLabel: Record<ManualType, string> = {
  DISPATCH_GUIDE: '긴급 출동', SYMPTOM_GUIDE: '증상', GENERAL: '일반',
};
const specialtyLabel: Record<string, string> = {
  KIOSK: '키오스크', ESPRESSO: '에스프레소 머신', ICE_MAKER: '제빙기', REFRIGERATOR: '냉장·냉동',
};

const inputStyle = {
  background: 'var(--muted)', border: '1px solid var(--border)',
  color: 'var(--foreground)', fontSize: 14, width: '100%',
};
const selectStyle = { ...inputStyle };

type Tab = 'courses' | 'manuals';

export default function SuperAdminLmsPage() {
  const { accessToken } = useAuth();
  const [tab, setTab] = useState<Tab>('courses');

  // 코스
  const [courses, setCourses] = useState<Course[]>([]);
  const [expandedCourseId, setExpandedCourseId] = useState<number | null>(null);
  const [lessons, setLessons] = useState<Record<number, Lesson[]>>({});
  const [showCourseForm, setShowCourseForm] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [courseForm, setCourseForm] = useState({ title: '', description: '', level: 'BEGINNER' as CourseLevel, targetSpecialty: '', passScore: '' });

  // 차시
  const [showLessonForm, setShowLessonForm] = useState<number | null>(null);
  const [editingLesson, setEditingLesson] = useState<Lesson | null>(null);
  const [lessonForm, setLessonForm] = useState({ title: '', content: '', orderIndex: '1' });

  // 매뉴얼
  const [manuals, setManuals] = useState<Manual[]>([]);
  const [showManualForm, setShowManualForm] = useState(false);
  const [editingManual, setEditingManual] = useState<Manual | null>(null);
  const [manualForm, setManualForm] = useState({ title: '', content: '', manualType: 'GENERAL' as ManualType, faultCategory: '' });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!accessToken) return;
    Promise.all([getCourses(accessToken), getManuals(accessToken)])
      .then(([c, m]) => { setCourses(c); setManuals(m); })
      .catch(() => setError('데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false));
  }, [accessToken]);

  async function toggleCourse(id: number) {
    if (expandedCourseId === id) { setExpandedCourseId(null); return; }
    setExpandedCourseId(id);
    if (!lessons[id] && accessToken) {
      const data = await getLessons(accessToken, id);
      setLessons(prev => ({ ...prev, [id]: data }));
    }
  }

  // 코스 저장
  async function handleSaveCourse() {
    if (!accessToken || !courseForm.title || !courseForm.level) return;
    const payload = {
      title: courseForm.title,
      description: courseForm.description || undefined,
      level: courseForm.level,
      targetSpecialty: courseForm.targetSpecialty || undefined,
      passScore: courseForm.passScore ? Number(courseForm.passScore) : undefined,
    };
    if (editingCourse) {
      const updated = await updateCourse(accessToken, editingCourse.id, payload);
      setCourses(prev => prev.map(c => c.id === updated.id ? updated : c));
    } else {
      const created = await createCourse(accessToken, payload);
      setCourses(prev => [...prev, created]);
    }
    resetCourseForm();
  }

  function startEditCourse(course: Course) {
    setEditingCourse(course);
    setCourseForm({
      title: course.title, description: course.description ?? '',
      level: course.level, targetSpecialty: course.targetSpecialty ?? '',
      passScore: course.passScore ? String(course.passScore) : '',
    });
    setShowCourseForm(true);
  }

  function resetCourseForm() {
    setShowCourseForm(false); setEditingCourse(null);
    setCourseForm({ title: '', description: '', level: 'BEGINNER', targetSpecialty: '', passScore: '' });
  }

  async function handleDeleteCourse(id: number) {
    if (!accessToken || !confirm('코스를 삭제하면 차시도 모두 삭제됩니다. 계속할까요?')) return;
    await deleteCourse(accessToken, id);
    setCourses(prev => prev.filter(c => c.id !== id));
    if (expandedCourseId === id) setExpandedCourseId(null);
  }

  // 차시 저장
  async function handleSaveLesson(courseId: number) {
    if (!accessToken || !lessonForm.title) return;
    const payload = {
      title: lessonForm.title,
      content: lessonForm.content || undefined,
      orderIndex: Number(lessonForm.orderIndex),
    };
    if (editingLesson) {
      const updated = await updateLesson(accessToken, editingLesson.id, payload);
      setLessons(prev => ({ ...prev, [courseId]: prev[courseId].map(l => l.id === updated.id ? updated : l) }));
    } else {
      const created = await createLesson(accessToken, courseId, payload);
      setLessons(prev => ({ ...prev, [courseId]: [...(prev[courseId] ?? []), created] }));
    }
    resetLessonForm();
  }

  function startEditLesson(lesson: Lesson) {
    setEditingLesson(lesson);
    setLessonForm({ title: lesson.title, content: lesson.content ?? '', orderIndex: String(lesson.orderIndex) });
    setShowLessonForm(lesson.courseId);
  }

  function resetLessonForm() {
    setShowLessonForm(null); setEditingLesson(null);
    setLessonForm({ title: '', content: '', orderIndex: '1' });
  }

  async function handleDeleteLesson(courseId: number, lessonId: number) {
    if (!accessToken || !confirm('차시를 삭제하시겠습니까?')) return;
    await deleteLesson(accessToken, lessonId);
    setLessons(prev => ({ ...prev, [courseId]: prev[courseId].filter(l => l.id !== lessonId) }));
  }

  // 매뉴얼 저장
  async function handleSaveManual() {
    if (!accessToken || !manualForm.title || !manualForm.content) return;
    const payload = {
      title: manualForm.title, content: manualForm.content,
      manualType: manualForm.manualType,
      faultCategory: manualForm.faultCategory || undefined,
    };
    if (editingManual) {
      const updated = await updateManual(accessToken, editingManual.id, payload);
      setManuals(prev => prev.map(m => m.id === updated.id ? updated : m));
    } else {
      const created = await createManual(accessToken, payload);
      setManuals(prev => [...prev, created]);
    }
    resetManualForm();
  }

  function startEditManual(manual: Manual) {
    setEditingManual(manual);
    setManualForm({ title: manual.title, content: manual.content, manualType: manual.manualType, faultCategory: manual.faultCategory ?? '' });
    setShowManualForm(true);
  }

  function resetManualForm() {
    setShowManualForm(false); setEditingManual(null);
    setManualForm({ title: '', content: '', manualType: 'GENERAL', faultCategory: '' });
  }

  async function handleDeleteManual(id: number) {
    if (!accessToken || !confirm('매뉴얼을 삭제하시겠습니까?')) return;
    await deleteManual(accessToken, id);
    setManuals(prev => prev.filter(m => m.id !== id));
  }

  if (loading) return <div style={{ color: 'var(--muted-foreground)', padding: 32 }}>불러오는 중...</div>;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 style={{ fontSize: 20, fontWeight: 700, color: 'var(--foreground)', letterSpacing: '-0.02em' }}>교육 콘텐츠 관리</h1>
        <p style={{ fontSize: 13, color: 'var(--muted-foreground)', marginTop: 2 }}>코스·차시·매뉴얼을 등록하고 관리합니다.</p>
      </div>

      {error && <p style={{ fontSize: 13, color: 'var(--destructive)' }}>{error}</p>}

      {/* 탭 */}
      <div className="flex gap-1 p-1 rounded-lg" style={{ background: 'var(--muted)', width: 'fit-content' }}>
        {(['courses', 'manuals'] as Tab[]).map(t => (
          <button key={t} onClick={() => setTab(t)} className="px-5 py-2 rounded-md transition-all"
            style={{
              fontSize: 14, fontWeight: tab === t ? 600 : 400,
              background: tab === t ? 'var(--card)' : 'transparent',
              color: tab === t ? 'var(--foreground)' : 'var(--muted-foreground)',
              boxShadow: tab === t ? '0 1px 3px rgba(0,0,0,0.1)' : 'none', cursor: 'pointer',
            }}>
            {t === 'courses' ? '코스' : '매뉴얼'}
          </button>
        ))}
      </div>

      {/* 코스·차시 탭 */}
      {tab === 'courses' && (
        <div className="flex flex-col gap-4">
          <div className="flex justify-between items-center">
            <span style={{ fontSize: 14, color: 'var(--muted-foreground)' }}>총 {courses.length}개 코스</span>
            <button onClick={() => { resetCourseForm(); setShowCourseForm(true); }}
              className="flex items-center gap-2 px-4 py-2 rounded-lg"
              style={{ background: 'var(--primary)', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
              <Plus size={14} /> 코스 등록
            </button>
          </div>

          {/* 코스 등록/수정 폼 */}
          {showCourseForm && (
            <div className="rounded-xl p-5 flex flex-col gap-3" style={{ background: 'var(--card)', border: '1px solid var(--primary)' }}>
              <div className="flex items-center justify-between">
                <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)' }}>{editingCourse ? '코스 수정' : '새 코스 등록'}</span>
                <button onClick={resetCourseForm} style={{ cursor: 'pointer', background: 'none', border: 'none' }}><X size={16} style={{ color: 'var(--muted-foreground)' }} /></button>
              </div>
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1">
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>코스명 *</label>
                  <input className="px-3 py-2 rounded-lg outline-none" style={inputStyle}
                    value={courseForm.title} onChange={e => setCourseForm(p => ({ ...p, title: e.target.value }))} placeholder="키오스크 수리 초급" />
                </div>
                <div className="flex flex-col gap-1" style={{ width: 120 }}>
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>등급 *</label>
                  <select className="px-3 py-2 rounded-lg outline-none" style={selectStyle}
                    value={courseForm.level} onChange={e => setCourseForm(p => ({ ...p, level: e.target.value as CourseLevel }))}>
                    <option value="BEGINNER">초급</option>
                    <option value="INTERMEDIATE">중급</option>
                    <option value="ADVANCED">고급</option>
                  </select>
                </div>
                <div className="flex flex-col gap-1" style={{ width: 140 }}>
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>전문분야</label>
                  <select className="px-3 py-2 rounded-lg outline-none" style={selectStyle}
                    value={courseForm.targetSpecialty} onChange={e => setCourseForm(p => ({ ...p, targetSpecialty: e.target.value }))}>
                    <option value="">전체</option>
                    <option value="KIOSK">키오스크</option>
                    <option value="ESPRESSO">에스프레소 머신</option>
                    <option value="ICE_MAKER">제빙기</option>
                    <option value="REFRIGERATOR">냉장·냉동</option>
                  </select>
                </div>
                <div className="flex flex-col gap-1" style={{ width: 100 }}>
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>합격점수</label>
                  <input type="number" className="px-3 py-2 rounded-lg outline-none" style={inputStyle}
                    value={courseForm.passScore} onChange={e => setCourseForm(p => ({ ...p, passScore: e.target.value }))} placeholder="70" min="0" max="100" />
                </div>
              </div>
              <div className="flex flex-col gap-1">
                <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>설명</label>
                <textarea className="px-3 py-2 rounded-lg outline-none resize-none" style={{ ...inputStyle, height: 72 }}
                  value={courseForm.description} onChange={e => setCourseForm(p => ({ ...p, description: e.target.value }))} placeholder="코스 설명을 입력하세요" />
              </div>
              <div className="flex justify-end">
                <button onClick={handleSaveCourse} className="flex items-center gap-2 px-4 py-2 rounded-lg"
                  style={{ background: 'var(--primary)', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                  <Check size={14} /> {editingCourse ? '수정 완료' : '등록'}
                </button>
              </div>
            </div>
          )}

          {/* 코스 목록 */}
          {courses.length === 0 ? (
            <div style={{ fontSize: 13, color: 'var(--muted-foreground)', padding: '20px 0' }}>등록된 코스가 없습니다.</div>
          ) : (
            <div className="flex flex-col gap-2">
              {courses.map(course => (
                <div key={course.id} className="rounded-xl overflow-hidden" style={{ border: '1px solid var(--border)' }}>
                  {/* 코스 헤더 */}
                  <div className="flex items-center gap-4 px-5 py-4 cursor-pointer"
                    style={{ background: 'var(--card)' }}
                    onClick={() => toggleCourse(course.id)}>
                    {expandedCourseId === course.id ? <ChevronDown size={16} style={{ color: 'var(--muted-foreground)', flexShrink: 0 }} /> : <ChevronRight size={16} style={{ color: 'var(--muted-foreground)', flexShrink: 0 }} />}
                    <BookOpen size={15} style={{ color: '#7C3AED', flexShrink: 0 }} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span style={{ fontSize: 15, fontWeight: 600, color: 'var(--foreground)' }}>{course.title}</span>
                        <span className="px-2 py-0.5 rounded-full" style={{ fontSize: 11, fontWeight: 600, background: '#7C3AED18', color: '#7C3AED' }}>{levelLabel[course.level]}</span>
                        {course.targetSpecialty && (
                          <span className="px-2 py-0.5 rounded-full" style={{ fontSize: 11, background: 'var(--muted)', color: 'var(--muted-foreground)' }}>{specialtyLabel[course.targetSpecialty] ?? course.targetSpecialty}</span>
                        )}
                      </div>
                      {course.description && <div style={{ fontSize: 12, color: 'var(--muted-foreground)', marginTop: 2 }}>{course.description}</div>}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--muted-foreground)', flexShrink: 0 }}>차시 {course.lessonCount}개</div>
                    <div className="flex gap-2 shrink-0" onClick={e => e.stopPropagation()}>
                      <button onClick={() => startEditCourse(course)} className="p-1.5 rounded-lg"
                        style={{ background: 'var(--muted)', cursor: 'pointer', border: 'none' }}>
                        <Pencil size={13} style={{ color: 'var(--muted-foreground)' }} />
                      </button>
                      <button onClick={() => handleDeleteCourse(course.id)} className="p-1.5 rounded-lg"
                        style={{ background: '#DC262618', cursor: 'pointer', border: 'none' }}>
                        <Trash2 size={13} style={{ color: '#DC2626' }} />
                      </button>
                    </div>
                  </div>

                  {/* 차시 목록 */}
                  {expandedCourseId === course.id && (
                    <div style={{ borderTop: '1px solid var(--border)', background: 'var(--background)' }}>
                      <div className="px-5 py-3 flex items-center justify-between">
                        <span style={{ fontSize: 13, color: 'var(--muted-foreground)', fontWeight: 500 }}>차시 목록</span>
                        <button onClick={() => { resetLessonForm(); setShowLessonForm(course.id); setLessonForm(p => ({ ...p, orderIndex: String((lessons[course.id]?.length ?? 0) + 1) })); }}
                          className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                          style={{ background: '#7C3AED18', color: '#7C3AED', fontSize: 12, fontWeight: 600, cursor: 'pointer', border: 'none' }}>
                          <Plus size={12} /> 차시 추가
                        </button>
                      </div>

                      {/* 차시 폼 */}
                      {showLessonForm === course.id && (
                        <div className="mx-5 mb-3 p-4 rounded-lg flex flex-col gap-3" style={{ background: 'var(--card)', border: '1px solid #7C3AED40' }}>
                          <div className="flex gap-3">
                            <div className="flex flex-col gap-1" style={{ width: 60 }}>
                              <label style={{ fontSize: 11, color: 'var(--muted-foreground)' }}>순서</label>
                              <input type="number" className="px-2 py-1.5 rounded-lg outline-none" style={inputStyle}
                                value={lessonForm.orderIndex} onChange={e => setLessonForm(p => ({ ...p, orderIndex: e.target.value }))} min="1" />
                            </div>
                            <div className="flex-1 flex flex-col gap-1">
                              <label style={{ fontSize: 11, color: 'var(--muted-foreground)' }}>차시 제목 *</label>
                              <input className="px-3 py-1.5 rounded-lg outline-none" style={inputStyle}
                                value={lessonForm.title} onChange={e => setLessonForm(p => ({ ...p, title: e.target.value }))} placeholder="키오스크 구조 이해" />
                            </div>
                          </div>
                          <div className="flex flex-col gap-1">
                            <label style={{ fontSize: 11, color: 'var(--muted-foreground)' }}>학습 내용</label>
                            <textarea className="px-3 py-2 rounded-lg outline-none resize-none" style={{ ...inputStyle, height: 80 }}
                              value={lessonForm.content} onChange={e => setLessonForm(p => ({ ...p, content: e.target.value }))} placeholder="차시 학습 내용을 입력하세요" />
                          </div>
                          <div className="flex justify-end gap-2">
                            <button onClick={resetLessonForm} className="px-3 py-1.5 rounded-lg"
                              style={{ background: 'var(--muted)', color: 'var(--muted-foreground)', fontSize: 12, cursor: 'pointer', border: 'none' }}>취소</button>
                            <button onClick={() => handleSaveLesson(course.id)} className="flex items-center gap-1 px-3 py-1.5 rounded-lg"
                              style={{ background: '#7C3AED', color: '#fff', fontSize: 12, fontWeight: 600, cursor: 'pointer', border: 'none' }}>
                              <Check size={12} /> {editingLesson ? '수정' : '추가'}
                            </button>
                          </div>
                        </div>
                      )}

                      {(lessons[course.id] ?? []).length === 0 ? (
                        <div style={{ fontSize: 12, color: 'var(--muted-foreground)', padding: '12px 20px 16px' }}>등록된 차시가 없습니다.</div>
                      ) : (
                        <div className="flex flex-col gap-1 px-5 pb-4">
                          {(lessons[course.id] ?? []).map(lesson => (
                            <div key={lesson.id} className="flex items-start gap-3 px-4 py-3 rounded-lg" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
                              <span className="shrink-0 w-6 h-6 rounded-full flex items-center justify-center" style={{ background: '#7C3AED18', color: '#7C3AED', fontSize: 11, fontWeight: 700 }}>{lesson.orderIndex}</span>
                              <div className="flex-1 min-w-0">
                                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--foreground)' }}>{lesson.title}</div>
                                {lesson.content && <div style={{ fontSize: 12, color: 'var(--muted-foreground)', marginTop: 2, whiteSpace: 'pre-wrap' }}>{lesson.content}</div>}
                              </div>
                              <div className="flex gap-1 shrink-0">
                                <button onClick={() => startEditLesson(lesson)} className="p-1.5 rounded-lg" style={{ background: 'var(--muted)', cursor: 'pointer', border: 'none' }}>
                                  <Pencil size={12} style={{ color: 'var(--muted-foreground)' }} />
                                </button>
                                <button onClick={() => handleDeleteLesson(course.id, lesson.id)} className="p-1.5 rounded-lg" style={{ background: '#DC262618', cursor: 'pointer', border: 'none' }}>
                                  <Trash2 size={12} style={{ color: '#DC2626' }} />
                                </button>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* 매뉴얼 탭 */}
      {tab === 'manuals' && (
        <div className="flex flex-col gap-4">
          <div className="flex justify-between items-center">
            <span style={{ fontSize: 14, color: 'var(--muted-foreground)' }}>총 {manuals.length}개 매뉴얼</span>
            <button onClick={() => { resetManualForm(); setShowManualForm(true); }}
              className="flex items-center gap-2 px-4 py-2 rounded-lg"
              style={{ background: 'var(--primary)', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
              <Plus size={14} /> 매뉴얼 등록
            </button>
          </div>

          {/* 매뉴얼 등록/수정 폼 */}
          {showManualForm && (
            <div className="rounded-xl p-5 flex flex-col gap-3" style={{ background: 'var(--card)', border: '1px solid var(--primary)' }}>
              <div className="flex items-center justify-between">
                <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--foreground)' }}>{editingManual ? '매뉴얼 수정' : '새 매뉴얼 등록'}</span>
                <button onClick={resetManualForm} style={{ cursor: 'pointer', background: 'none', border: 'none' }}><X size={16} style={{ color: 'var(--muted-foreground)' }} /></button>
              </div>
              <div className="flex gap-3">
                <div className="flex-1 flex flex-col gap-1">
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>제목 *</label>
                  <input className="px-3 py-2 rounded-lg outline-none" style={inputStyle}
                    value={manualForm.title} onChange={e => setManualForm(p => ({ ...p, title: e.target.value }))} placeholder="긴급 출동 대응 매뉴얼" />
                </div>
                <div className="flex flex-col gap-1" style={{ width: 140 }}>
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>유형 *</label>
                  <select className="px-3 py-2 rounded-lg outline-none" style={selectStyle}
                    value={manualForm.manualType} onChange={e => setManualForm(p => ({ ...p, manualType: e.target.value as ManualType }))}>
                    <option value="DISPATCH_GUIDE">긴급 출동</option>
                    <option value="SYMPTOM_GUIDE">증상</option>
                    <option value="GENERAL">일반</option>
                  </select>
                </div>
                <div className="flex flex-col gap-1" style={{ width: 160 }}>
                  <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>고장 카테고리</label>
                  <input className="px-3 py-2 rounded-lg outline-none" style={inputStyle}
                    value={manualForm.faultCategory} onChange={e => setManualForm(p => ({ ...p, faultCategory: e.target.value }))} placeholder="화면손상 (선택)" />
                </div>
              </div>
              <div className="flex flex-col gap-1">
                <label style={{ fontSize: 12, color: 'var(--muted-foreground)' }}>내용 *</label>
                <textarea className="px-3 py-2 rounded-lg outline-none resize-none" style={{ ...inputStyle, height: 120 }}
                  value={manualForm.content} onChange={e => setManualForm(p => ({ ...p, content: e.target.value }))} placeholder="매뉴얼 내용을 입력하세요" />
              </div>
              <div className="flex justify-end">
                <button onClick={handleSaveManual} className="flex items-center gap-2 px-4 py-2 rounded-lg"
                  style={{ background: 'var(--primary)', color: '#fff', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                  <Check size={14} /> {editingManual ? '수정 완료' : '등록'}
                </button>
              </div>
            </div>
          )}

          {/* 매뉴얼 목록 */}
          {manuals.length === 0 ? (
            <div style={{ fontSize: 13, color: 'var(--muted-foreground)', padding: '20px 0' }}>등록된 매뉴얼이 없습니다.</div>
          ) : (
            <div className="flex flex-col gap-2">
              {manuals.map(manual => (
                <div key={manual.id} className="rounded-xl p-5" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
                  <div className="flex items-start gap-3">
                    <FileText size={15} style={{ color: '#2563EB', flexShrink: 0, marginTop: 2 }} />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span style={{ fontSize: 15, fontWeight: 600, color: 'var(--foreground)' }}>{manual.title}</span>
                        <span className="px-2 py-0.5 rounded-full" style={{ fontSize: 11, fontWeight: 600, background: '#2563EB18', color: '#2563EB' }}>{manualTypeLabel[manual.manualType]}</span>
                        {manual.faultCategory && (
                          <span className="px-2 py-0.5 rounded-full" style={{ fontSize: 11, background: 'var(--muted)', color: 'var(--muted-foreground)' }}>{manual.faultCategory}</span>
                        )}
                      </div>
                      <div style={{ fontSize: 13, color: 'var(--muted-foreground)', whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>{manual.content}</div>
                    </div>
                    <div className="flex gap-2 shrink-0">
                      <button onClick={() => startEditManual(manual)} className="p-1.5 rounded-lg" style={{ background: 'var(--muted)', cursor: 'pointer', border: 'none' }}>
                        <Pencil size={13} style={{ color: 'var(--muted-foreground)' }} />
                      </button>
                      <button onClick={() => handleDeleteManual(manual.id)} className="p-1.5 rounded-lg" style={{ background: '#DC262618', cursor: 'pointer', border: 'none' }}>
                        <Trash2 size={13} style={{ color: '#DC2626' }} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
