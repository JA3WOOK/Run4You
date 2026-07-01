import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

function authHeader(token: string) {
  return { Authorization: `Bearer ${token}` };
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  name: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
  role: 'STORE_OWNER' | 'ENGINEER';
  brandId: number;
  specialties?: string[];
}

export interface BrandSignupRequest {
  brandName: string;
  businessNo: string;
  commissionRate: number;
  adminEmail: string;
  adminPassword: string;
  adminName: string;
  adminPhone?: string;
}

export async function login(email: string, password: string): Promise<TokenResponse> {
  const res = await api.post('/auth/login', { email, password });
  return res.data.data;
}

export async function signup(data: SignupRequest): Promise<void> {
  await api.post('/auth/signup', data);
}

export async function signupBrand(data: BrandSignupRequest): Promise<void> {
  await api.post('/auth/signup/brand', data);
}

export async function logout(token: string): Promise<void> {
  await api.post('/auth/logout', {}, { headers: authHeader(token) });
}

export interface MyProfile {
  id: number;
  email: string;
  name: string;
  phone: string | null;
  role: string;
  status: string;
  brandId: number | null;
  brandName: string | null;
  specialties: string[];
  rating: number | null;
  skillGrade: string | null;
}

export async function getMyProfile(token: string): Promise<MyProfile> {
  const res = await api.get('/users/me', { headers: authHeader(token) });
  return res.data.data;
}

export interface ActiveBrand {
  id: number;
  name: string;
}

export async function getActiveBrands(): Promise<ActiveBrand[]> {
  const res = await api.get('/brands/active');
  return res.data.data;
}

export async function updateMyProfile(token: string, data: {
  name?: string;
  phone?: string;
  currentPassword?: string;
  newPassword?: string;
}): Promise<MyProfile> {
  const res = await api.patch('/users/me', data, { headers: authHeader(token) });
  return res.data.data;
}
