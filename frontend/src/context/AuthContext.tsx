import { useState, useEffect, type ReactNode } from "react";
import { tokenStorage, storage } from "@/utils/storage";
import type { User } from "@/types/auth.types";
import { AuthContext, type AuthContextType } from "./auth.context";

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [user, setUserState] = useState<User | null>(() =>
    storage.get<User>("user"),
  );

  // Computed from token presence
  const isAuthenticated = !!tokenStorage.getToken();

  const setTokens = (accessToken: string, refreshToken: string) => {
    tokenStorage.setToken(accessToken);
    tokenStorage.setRefreshToken(refreshToken);
  };

  const clearAuth = () => {
    tokenStorage.removeToken();
    tokenStorage.removeRefreshToken();
    storage.remove("user");
    setUserState(null);
  };

  const setUser = (newUser: User) => {
    storage.set("user", newUser);
    setUserState(newUser);
  };

  useEffect(() => {
    const storedUser = storage.get<User>("user");
    if (storedUser) {
      setUserState(storedUser);
    }
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    setTokens,
    clearAuth,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
