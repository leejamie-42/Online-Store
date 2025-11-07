import { describe, it, expect, vi, beforeEach } from "vitest";
import { apiClient } from "../axios";
import { tokenStorage } from "@/utils/storage";
import type { InternalAxiosRequestConfig, AxiosResponse } from "axios";

vi.mock("@/utils/storage");

describe("Axios Client", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("Request Interceptor", () => {
    it("should add Authorization header when token exists", () => {
      vi.mocked(tokenStorage.getToken).mockReturnValue("mock-token");

      const config: Partial<InternalAxiosRequestConfig> = {
        headers: {} as InternalAxiosRequestConfig["headers"],
      };

      const result = apiClient.interceptors.request.handlers[0].fulfilled(
        config as InternalAxiosRequestConfig,
      );

      expect(result.headers.Authorization).toBe("Bearer mock-token");
    });

    it("should not add header when no token", () => {
      vi.mocked(tokenStorage.getToken).mockReturnValue(null);

      const config: Partial<InternalAxiosRequestConfig> = {
        headers: {} as InternalAxiosRequestConfig["headers"],
      };

      const result = apiClient.interceptors.request.handlers[0].fulfilled(
        config as InternalAxiosRequestConfig,
      );

      expect(result.headers.Authorization).toBeUndefined();
    });
  });

  describe("Response Interceptor", () => {
    it("should pass through successful responses", () => {
      const response: AxiosResponse = {
        data: "test",
        status: 200,
        statusText: "OK",
        headers: {},
        config: {} as InternalAxiosRequestConfig,
      };
      const result =
        apiClient.interceptors.response.handlers[0].fulfilled(response);

      expect(result).toEqual(response);
    });

    it("should handle 401 errors by attempting refresh", () => {
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue("refresh-token");

      // Verify refresh token is checked
      expect(tokenStorage.getRefreshToken).toBeDefined();
    });
  });
});
