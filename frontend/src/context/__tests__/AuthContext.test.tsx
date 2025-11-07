import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "../AuthContext";
import { useAuth } from "@/hooks/useAuth";
import { tokenStorage, storage } from "@/utils/storage";
import type { ReactNode } from "react";

vi.mock("@/utils/storage");

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
};

describe("AuthContext", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(tokenStorage.getToken).mockReturnValue(null);
    vi.mocked(storage.get).mockReturnValue(null);
  });

  it("should provide isAuthenticated based on token", () => {
    vi.mocked(tokenStorage.getToken).mockReturnValue("mock-token");

    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isAuthenticated).toBe(true);
  });

  it("should store tokens on setTokens", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.setTokens("access-token", "refresh-token");
    });

    expect(tokenStorage.setToken).toHaveBeenCalledWith("access-token");
    expect(tokenStorage.setRefreshToken).toHaveBeenCalledWith("refresh-token");
  });

  it("should clear auth on clearAuth", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.clearAuth();
    });

    expect(tokenStorage.removeToken).toHaveBeenCalled();
    expect(tokenStorage.removeRefreshToken).toHaveBeenCalled();
    expect(storage.remove).toHaveBeenCalledWith("user");
  });

  it("should set user on setUser", () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: createWrapper(),
    });
    const mockUser = {
      id: 1,
      name: "Test",
      email: "test@e.com",
      role: "CUSTOMER" as const,
    };

    act(() => {
      result.current.setUser(mockUser);
    });

    expect(storage.set).toHaveBeenCalledWith("user", mockUser);
    expect(result.current.user).toEqual(mockUser);
  });
});
