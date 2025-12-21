import React, { createContext, useContext, useState, useEffect } from "react";
import {
  authService,
  type LoginRequest,
  type RegisterRequest,
} from "@/services/auth.service";

interface User {
  username: string;
  fullName: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(
    localStorage.getItem("token")
  );

  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user"); // We might want to persist user info too
    if (storedToken) {
      setToken(storedToken);
    }
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        console.error("Failed to parse user from local storage", e);
      }
    }
  }, []);

  const login = async (data: LoginRequest) => {
    try {
      const response = await authService.login(data);
      if (response.errorCode === 100000 && response.data) {
        const { token, userInfo } = response.data;
        setToken(token);
        setUser(userInfo);
        localStorage.setItem("token", token);
        localStorage.setItem("user", JSON.stringify(userInfo));
      } else {
        throw new Error(response.errorDescription || "Login failed");
      }
    } catch (error) {
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      const response = await authService.register(data);
      if (response.errorCode !== 100000) {
        throw new Error(response.errorDescription || "Registration failed");
      }
      // Registration success typically doesn't auto-login in this API desc,
      // but we can ask user to login or auto-login if API returned token (it doesn't seem to).
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider
      value={{ user, token, login, register, logout, isAuthenticated: !!token }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
