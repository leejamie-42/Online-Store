// Re-export all types for easier importing

// Auth types (primary export)
export type {
  User,
  UserRole,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  LogoutRequest,
  AuthErrorResponse,
} from "./auth.types";

// User types (legacy support)
export type {
  Address,
  LoginCredentials, // Deprecated
  RegisterData, // Deprecated
} from "./user.types";

// Other types
export * from "./common.types";
export * from "./api.types";

export * from "./order.types";
export * from "./payment.types";
