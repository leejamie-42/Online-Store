// Re-export auth types for backward compatibility
export type { User, UserRole } from "./auth.types";

export interface Address {
  id?: string;
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone: string;
}

// DEPRECATED: Use LoginRequest from auth.types instead
/** @deprecated Use LoginRequest from auth.types */
export interface LoginCredentials {
  email: string;
  password: string;
}

// DEPRECATED: Use RegisterRequest from auth.types instead
/** @deprecated Use RegisterRequest from auth.types */
export interface RegisterData {
  username: string;
  email: string;
  password: string;
}
