import axios, { type AxiosInstance } from 'axios';

export const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export function setupUnauthorizedHandler(handler: () => void): number {
  return apiClient.interceptors.response.use(
    res => res,
    err => {
      if (err.response?.status === 401) {
        handler();
      }
      return Promise.reject(err);
    }
  );
}
