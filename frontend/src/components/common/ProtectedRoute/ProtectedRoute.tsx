import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/config/routes";
import type { UserRole } from "@/types/auth.types";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requiredRole?: UserRole;
}

/**
 * ProtectedRoute Component
 *
 * Protects routes based on authentication status and user roles
 * - Redirects to login if not authenticated
 * - Redirects to home if role requirements not met
 * - Preserves intended destination in location state
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
  requiredRole,
}) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  // Check authentication requirement
  if (requireAuth && !isAuthenticated) {
    // Redirect to login with return URL
    return (
      <Navigate to={ROUTES.LOGIN} state={{ from: location.pathname }} replace />
    );
  }

  // Check role requirement
  if (requiredRole && user?.role !== requiredRole) {
    // User doesn't have required role - redirect to home
    return <Navigate to={ROUTES.HOME} replace />;
  }

  // All checks passed - render protected content
  return <>{children}</>;
};

export default ProtectedRoute;
