import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { authService } from "@/api/services/auth.service";
import { useAuth } from "@/hooks/useAuth";
import type { LoginRequest, RegisterRequest } from "@/types/auth.types";

/**
 * Login Mutation Hook
 */
export function useLoginMutation() {
  const { setTokens, setUser } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (credentials: LoginRequest) => authService.login(credentials),

    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      navigate("/");
    },

    onError: (error) => {
      console.error("Login failed:", error);
    },
  });
}

/**
 * Register Mutation Hook
 */
export function useRegisterMutation() {
  const { setTokens, setUser } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (userData: RegisterRequest) => authService.register(userData),

    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      navigate("/");
    },

    onError: (error) => {
      console.error("Registration failed:", error);
    },
  });
}

/**
 * Logout Mutation Hook
 */
export function useLogoutMutation() {
  const { clearAuth } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (refreshToken: string) => authService.logout(refreshToken),

    onSuccess: () => {
      clearAuth();
      navigate("/login");
    },

    onError: (error) => {
      console.error("Logout failed, clearing local state:", error);
      clearAuth();
      navigate("/login");
    },
  });
}

/**
 * All auth mutations
 */
export function useAuthMutations() {
  return {
    loginMutation: useLoginMutation(),
    registerMutation: useRegisterMutation(),
    logoutMutation: useLogoutMutation(),
  };
}
