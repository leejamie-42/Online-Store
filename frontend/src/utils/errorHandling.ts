/**
 * Error Handling Utilities
 * Provides consistent error messages for API failures
 */

export interface ApiError {
  message: string;
  status?: number;
  details?: string;
}

/**
 * Extract error message from various error types
 */
export const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === "string") {
    return error;
  }
  return "An unexpected error occurred";
};

/**
 * Handle API errors and return user-friendly messages
 */
export const handleApiError = (error: unknown): ApiError => {
  // Axios error with response
  if (error && typeof error === "object" && "response" in error) {
    const axiosError = error as {
      response?: { status?: number; data?: { message?: string } };
    };
    const status = axiosError.response?.status;
    const data = axiosError.response?.data;

    switch (status) {
      case 404:
        return {
          message: "Product not found",
          status: 404,
          details: "The requested product does not exist or has been removed.",
        };
      case 500:
        return {
          message: "Server error",
          status: 500,
          details: "An error occurred on the server. Please try again later.",
        };
      case 503:
        return {
          message: "Service unavailable",
          status: 503,
          details:
            "The service is temporarily unavailable. Please try again later.",
        };
      default:
        return {
          message: data?.message || "Request failed",
          status,
          details: "An error occurred while processing your request.",
        };
    }
  }

  // Network error
  if (error && typeof error === "object" && "message" in error) {
    const errorMessage = (error as Error).message;
    if (
      errorMessage.includes("Network Error") ||
      errorMessage.includes("ERR_CONNECTION_REFUSED")
    ) {
      return {
        message: "Cannot connect to server",
        details:
          "Please ensure the backend server is running on http://localhost:8081",
      };
    }
  }

  // Generic error
  return {
    message: getErrorMessage(error),
    details: "Please try again or contact support if the problem persists.",
  };
};

/**
 * Product-specific error messages
 */
export const getProductErrorMessage = (
  error: unknown,
  productId?: number,
): string => {
  const apiError = handleApiError(error);

  if (apiError.status === 404 && productId) {
    return `Product #${productId} not found`;
  }

  return apiError.message;
};
