import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080/api' });

export interface BrandOption {
  id: number;
  name: string;
}

export async function getActiveBrands(token: string): Promise<BrandOption[]> {
  const res = await api.get('/brands', {
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.data.data.filter((b: { status: string }) => b.status === 'ACTIVE');
}
