import { describe, it, expect } from "vitest";
import type { User, LoginRequest, LoginResponse } from "../auth.types";

describe("Auth Type Definitions", () => {
  describe("LoginRequest", () => {
    it("should match backend contract with username field", () => {
      const loginRequest: LoginRequest = {
        username: "customer@example.com",
        password: "COMP5348",
      };

      expect(loginRequest).toHaveProperty("username");
      expect(loginRequest).toHaveProperty("password");
    });
  });

  describe("LoginResponse", () => {
    it("should match backend JWT response structure", () => {
      const loginResponse: LoginResponse = {
        accessToken: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        refreshToken: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        user: {
          id: 1,
          name: "Customer User",
          email: "customer@example.com",
          role: "CUSTOMER",
        },
      };

      expect(loginResponse).toHaveProperty("accessToken");
      expect(loginResponse).toHaveProperty("refreshToken");
      expect(loginResponse).toHaveProperty("user");
      expect(loginResponse.user.id).toBeTypeOf("number");
    });
  });

  describe("User", () => {
    it("should have numeric ID matching backend Long type", () => {
      const user: User = {
        id: 1,
        name: "Test User",
        email: "test@example.com",
        role: "CUSTOMER",
      };

      expect(user.id).toBeTypeOf("number");
      expect(user).toHaveProperty("name");
      expect(user).toHaveProperty("email");
      expect(user).toHaveProperty("role");
    });

    it("should support all role types", () => {
      const customer: User = {
        id: 1,
        name: "C",
        email: "c@e.com",
        role: "CUSTOMER",
      };
      const admin: User = { id: 2, name: "A", email: "a@e.com", role: "ADMIN" };
      const manager: User = {
        id: 3,
        name: "M",
        email: "m@e.com",
        role: "MANAGER",
      };

      expect(customer.role).toBe("CUSTOMER");
      expect(admin.role).toBe("ADMIN");
      expect(manager.role).toBe("MANAGER");
    });
  });
});
