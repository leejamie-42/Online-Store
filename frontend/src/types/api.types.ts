import type { ApiError, ApiResponse, PaginatedResponse } from './common.types';

export type { ApiError, ApiResponse, PaginatedResponse };

export interface RequestConfig {
  headers?: Record<string, string>;
  params?: Record<string, unknown>;
  timeout?: number;
}
