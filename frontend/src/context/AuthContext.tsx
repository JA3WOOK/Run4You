import React, { createContext, useContext, useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { logout } from '../api/auth';

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
  const [accessToken, setAccessToken] = useState<string | null>(
    localStorage.getItem('accessToken')
  );
  const [user, setUser] = useState<AuthUser | null>(() => {
    const token = localStorage.getItem('accessToken');
    const name = localStorage.getItem('userName') ?? '';
    return token ? parseToken(token, name) : null;
  });

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
