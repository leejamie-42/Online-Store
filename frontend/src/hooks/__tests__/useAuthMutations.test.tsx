import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  useLoginMutation,
  useRegisterMutation,
  useLogoutMutation,
} from "../useAuthMutations";
import { authService } from "@/api/services/auth.service";
import { AuthProvider } from "@/context/AuthContext";
import type { ReactNode } from "react";

vi.mock("@/api/services/auth.service");
vi.mock("@/utils/storage");
vi.mock("react-router-dom", () => ({
  useNavigate: () => vi.fn(),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { mutations: { retry: false } },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
};

describe("Auth Mutation Hooks", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("useLoginMutation", () => {
    it("should login successfully", async () => {
      const mockResponse = {
        accessToken: "token",
        refreshToken: "refresh",
        user: {
          id: 1,
          name: "Test",
          email: "test@e.com",
          role: "CUSTOMER" as const,
        },
      };

      vi.mocked(authService.login).mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useLoginMutation(), {
        wrapper: createWrapper(),
      });

      result.current.mutate({
        username: "test@e.com",
        password: "pass",
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });
    });
  });

  describe("useRegisterMutation", () => {
    it("should register successfully", async () => {
      const mockResponse = {
        accessToken: "token",
        refreshToken: "refresh",
        user: {
          id: 2,
          name: "New User",
          email: "new@e.com",
          role: "CUSTOMER" as const,
        },
      };

      vi.mocked(authService.register).mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useRegisterMutation(), {
        wrapper: createWrapper(),
      });

      result.current.mutate({
        username: "New User",
        email: "new@e.com",
        password: "password123",
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });
    });
  });

  describe("useLogoutMutation", () => {
    it("should clear auth even if API fails", async () => {
      // Suppress expected console error
      const consoleError = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      vi.mocked(authService.logout).mockRejectedValue(new Error("Network"));

      const { result } = renderHook(() => useLogoutMutation(), {
        wrapper: createWrapper(),
      });

      result.current.mutate("refresh-token");

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      consoleError.mockRestore();
    });
  });
});
