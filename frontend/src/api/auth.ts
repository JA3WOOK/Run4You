import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
  role: 'STORE_OWNER' | 'ENGINEER';
  brandId: number;
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
