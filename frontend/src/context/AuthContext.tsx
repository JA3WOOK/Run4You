import React, { createContext, useContext, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  role: string;
  exp: number;
}

export interface AuthUser {
  email: string;
  role: string;
}

interface AuthContextType {
  user: AuthUser | null;
  accessToken: string | null;
  signIn: (accessToken: string, refreshToken: string) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

function parseToken(token: string): AuthUser {
  const decoded = jwtDecode<JwtPayload>(token);
  return { email: decoded.sub, role: decoded.role };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(
    localStorage.getItem('accessToken')
  );
  const [user, setUser] = useState<AuthUser | null>(() => {
    const token = localStorage.getItem('accessToken');
    return token ? parseToken(token) : null;
  });

  function signIn(accessToken: string, refreshToken: string) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    setAccessToken(accessToken);
    setUser(parseToken(accessToken));
  }

  function signOut() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
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
