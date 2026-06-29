import React, { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { logout } from '../api/auth';
import { apiClient, setupUnauthorizedHandler } from '../api/apiClient';

interface JwtPayload {
  sub: string;
  role: string;
  exp: number;
}

export interface AuthUser {
  email: string;
  role: string;
  name: string;
}

interface AuthContextType {
  user: AuthUser | null;
  accessToken: string | null;
  signIn: (accessToken: string, refreshToken: string, name: string) => void;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

function parseToken(token: string, name: string): AuthUser {
  const decoded = jwtDecode<JwtPayload>(token);
  return { email: decoded.sub, role: decoded.role, name };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const [accessToken, setAccessToken] = useState<string | null>(
    localStorage.getItem('accessToken')
  );
  const [user, setUser] = useState<AuthUser | null>(() => {
    const token = localStorage.getItem('accessToken');
    const name = localStorage.getItem('userName') ?? '';
    return token ? parseToken(token, name) : null;
  });

  useEffect(() => {
    const id = setupUnauthorizedHandler(() => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('userName');
      setAccessToken(null);
      setUser(null);
      navigate('/login', { replace: true });
    });
    return () => apiClient.interceptors.response.eject(id);
  }, []);

  function signIn(accessToken: string, refreshToken: string, name: string) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('userName', name);
    setAccessToken(accessToken);
    setUser(parseToken(accessToken, name));
  }

  async function signOut() {
    if (accessToken) {
      try {
        await logout(accessToken);
      } catch {
        // 서버 호출 실패해도 로컬 상태는 정리
      }
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userName');
    setAccessToken(null);
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, accessToken, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
