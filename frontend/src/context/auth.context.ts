import { createContext } from "react";
import type { User } from "@/types/auth.types";

/**
 * AuthContext - Client State Management
 *
 * Manages tokens and user state (client state)
 * Server state (login, register, logout) handled by React Query mutations
 */
export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  setTokens: (accessToken: string, refreshToken: string) => void;
  clearAuth: () => void;
  setUser: (user: User) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
