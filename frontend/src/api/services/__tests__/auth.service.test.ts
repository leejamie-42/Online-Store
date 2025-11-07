import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { authService } from "../auth.service";
import { apiClient } from "@/lib/axios";
import { API_ENDPOINTS } from "@/config/api.config";
import type { LoginRequest, RegisterRequest } from "@/types/auth.types";

vi.mock("@/lib/axios");

describe("Auth Service", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe("login", () => {
    const mockLoginRequest: LoginRequest = {
      username: "customer@example.com",
      password: "COMP5348",
    };

    const mockLoginResponse = {
      accessToken: "eyJhbGc...",
      refreshToken: "eyJhbGc...",
      user: {
        id: 1,
        name: "Customer User",
        email: "customer@example.com",
        role: "CUSTOMER" as const,
      },
    };

    it("should login with valid credentials", async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockLoginResponse });

      const result = await authService.login(mockLoginRequest);

      expect(apiClient.post).toHaveBeenCalledWith(
        API_ENDPOINTS.LOGIN,
        mockLoginRequest,
      );
      expect(result).toEqual(mockLoginResponse);
      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBeDefined();
    });

    it("should throw error on invalid credentials", async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 401, data: { error: "Unauthorized" } },
      });

      await expect(authService.login(mockLoginRequest)).rejects.toThrow();
    });

    it("should handle network errors", async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error("Network error"));

      await expect(authService.login(mockLoginRequest)).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("register", () => {
    const mockRegisterRequest: RegisterRequest = {
      name: "John Doe",
      email: "john@example.com",
      password: "SecurePass123!",
    };

    const mockRegisterResponse = {
      accessToken: "eyJhbGc...",
      refreshToken: "eyJhbGc...",
      user: {
        id: 2,
        name: "John Doe",
        email: "john@example.com",
        role: "CUSTOMER" as const,
      },
    };

    it("should register new user successfully", async () => {
      vi.mocked(apiClient.post).mockResolvedValue({
        data: mockRegisterResponse,
      });

      const result = await authService.register(mockRegisterRequest);

      expect(apiClient.post).toHaveBeenCalledWith(
        API_ENDPOINTS.REGISTER,
        mockRegisterRequest,
      );
      expect(result.user.role).toBe("CUSTOMER");
    });

    it("should throw error when email exists", async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 409, data: { error: "Conflict" } },
      });

      await expect(authService.register(mockRegisterRequest)).rejects.toThrow();
    });
  });

  describe("refreshToken", () => {
    it("should refresh access token", async () => {
      const mockResponse = { accessToken: "new-token" };
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse });

      const result = await authService.refreshToken("refresh-token");

      expect(result.accessToken).toBeDefined();
      expect(result).not.toHaveProperty("refreshToken");
    });

    it("should throw on invalid refresh token", async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 401 },
      });

      await expect(authService.refreshToken("invalid")).rejects.toThrow();
    });
  });

  describe("logout", () => {
    it("should logout successfully", async () => {
      vi.mocked(apiClient.post).mockResolvedValue({
        data: { message: "Success" },
      });

      await authService.logout("refresh-token");

      expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.LOGOUT, {
        refreshToken: "refresh-token",
      });
    });
  });
});
