import apiClient from "./api.client";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  data?: {
    token: string;
    userInfo: {
      username: string;
      fullName: string;
      createdAt: string;
      updatedAt: string;
    };
  };
  errorCode: number;
  errorDescription: string;
  httpCode: number;
  trace?: string;
}

export const authService = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    return apiClient.post("/api/users/login", data);
  },
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    return apiClient.post("/api/users/create_new", data);
  },
};
