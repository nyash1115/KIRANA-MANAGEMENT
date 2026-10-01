import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, ApiResponse } from '../types';
import api from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isManager: boolean;
  isCashier: boolean;
  storeId: number;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('kirana_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('kirana_token');
  });

  const storeId = user?.storeId || 1;

  const login = async (username: string, password: string) => {
    const response = await api.post<ApiResponse<any>>('/auth/login', { username, password });
    const authData = response.data.data;
    const userData: User = {
      id: authData.userId,
      username: authData.username,
      fullName: authData.fullName,
      role: authData.role,
      storeId: authData.storeId,
      businessId: authData.businessId,
      token: authData.token,
    };

    localStorage.setItem('kirana_token', authData.token);
    localStorage.setItem('kirana_user', JSON.stringify(userData));
    setToken(authData.token);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('kirana_token');
    localStorage.removeItem('kirana_user');
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';
  const isManager = user?.role === 'ROLE_MANAGER';
  const isCashier = user?.role === 'ROLE_CASHIER';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isAdmin,
        isManager,
        isCashier,
        storeId,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
