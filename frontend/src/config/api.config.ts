/**
 * API Configuration
 *
 * Backend API runs on port 8081 (configured in application-local.yml: server.port: 8081)
 * All endpoints are prefixed with /api in backend routes
 *
 * Reference: CLAUDE.md, AUTHENTICATION_FLOW.md
 */
export const API_CONFIG = {
  // Backend runs on port 8081 (see store-backend/src/main/resources/application-local.yml)
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api",
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT as string) || 15000,
} as const;

/**
 * API Endpoints
 *
 * All auth endpoints match backend AuthenticationController routes
 */
export const API_ENDPOINTS = {
  // Authentication endpoints (AuthenticationController)
  REGISTER: "/auth/register", // POST /api/v1/auth/register
  LOGIN: "/auth/login", // POST /api/v1/auth/login
  REFRESH_TOKEN: "/auth/refresh", // POST /api/v1/auth/refresh
  LOGOUT: "/auth/logout", // POST /api/v1/auth/logout

  // Product endpoints
  PRODUCTS: "/products",
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  ORDERS: "/orders",
  ORDER_DETAIL: (orderId: string) => `/orders/${orderId}`,
  CANCEL_ORDER: (orderId: string) => `/orders/${orderId}/cancel`,
} as const;

/**
 * API Endpoint Type Guards
 */
export const isAuthEndpoint = (endpoint: string): boolean => {
  return endpoint.startsWith("/auth/");
};

export const isProtectedEndpoint = (endpoint: string): boolean => {
  const publicEndpoints = [
    API_ENDPOINTS.LOGIN,
    API_ENDPOINTS.REGISTER,
    API_ENDPOINTS.REFRESH_TOKEN,
    API_ENDPOINTS.PRODUCTS,
  ];

  return !publicEndpoints.some(
    (publicEndpoint) =>
      typeof publicEndpoint === "string" && endpoint === publicEndpoint,
  );
};
