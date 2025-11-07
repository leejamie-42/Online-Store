import { apiClient } from "@/lib/axios";
import { API_ENDPOINTS } from "@/config/api.config";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenResponse,
} from "@/types/auth.types";

/**
 * Authentication Service
 *
 * Handles all authentication-related API calls
 * Backend documentation: docs/AUTHENTICATION_FLOW.md
 */
export const authService = {
  /**
   * Login with credentials
   * POST /api/auth/login
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
      API_ENDPOINTS.LOGIN,
      credentials,
    );
    return response.data;
  },

  /**
   * Register new user
   * POST /api/auth/register
   * Backend auto-logs in user after registration
   */
  async register(userData: RegisterRequest): Promise<RegisterResponse> {
    const response = await apiClient.post<RegisterResponse>(
      API_ENDPOINTS.REGISTER,
      userData,
    );
    return response.data;
  },

  /**
   * Refresh access token
   * POST /api/auth/refresh
   * Returns new access token only
   */
  async refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
    const response = await apiClient.post<RefreshTokenResponse>(
      API_ENDPOINTS.REFRESH_TOKEN,
      { refreshToken },
    );
    return response.data;
  },

  /**
   * Logout and invalidate tokens
   * POST /api/auth/logout
   * Blacklists access token, deletes refresh token
   */
  async logout(refreshToken: string): Promise<void> {
    await apiClient.post(API_ENDPOINTS.LOGOUT, { refreshToken });
  },
};
