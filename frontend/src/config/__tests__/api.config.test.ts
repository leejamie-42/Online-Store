import { describe, it, expect } from "vitest";
import {
  API_CONFIG,
  API_ENDPOINTS,
  isAuthEndpoint,
  isProtectedEndpoint,
} from "../api.config";

describe("API Configuration", () => {
  describe("API_CONFIG", () => {
    it("should use port 8081 for backend", () => {
      expect(API_CONFIG.baseURL).toContain("8081");
    });

    it("should have correct timeout", () => {
      expect(API_CONFIG.timeout).toBe(15000);
    });
  });

  describe("Auth Endpoints", () => {
    it("should have all auth endpoints", () => {
      expect(API_ENDPOINTS.LOGIN).toBe("/auth/login");
      expect(API_ENDPOINTS.REGISTER).toBe("/auth/register");
      expect(API_ENDPOINTS.REFRESH_TOKEN).toBe("/auth/refresh");
      expect(API_ENDPOINTS.LOGOUT).toBe("/auth/logout");
    });
  });

  describe("Helper Functions", () => {
    it("should identify auth endpoints", () => {
      expect(isAuthEndpoint("/auth/login")).toBe(true);
      expect(isAuthEndpoint("/products")).toBe(false);
    });

    it("should identify protected endpoints", () => {
      expect(isProtectedEndpoint("/orders")).toBe(true);
      expect(isProtectedEndpoint("/auth/login")).toBe(false);
    });
  });
});
