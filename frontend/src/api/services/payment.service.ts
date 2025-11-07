/**
 * Payment API Service
 * Handles all payment-related API calls
 */

import { apiClient } from "@/lib/axios";
import type {
  CreatePaymentRequest,
  CreatePaymentResponse,
  BpayInfoResponse,
  RefundResponse,
} from "@/types";
import { getMockBpayDetails } from "@/mocks/payment.mock";

/**
 * Check if we should use mock data
 */
const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === "true";

/**
 * Payment API Service
 * Provides methods for payment creation and BPAY information retrieval
 */
export const paymentService = {
  /**
   * Create new payment
   * POST /api/payments
   */
  createPayment: async (
    data: CreatePaymentRequest,
  ): Promise<CreatePaymentResponse> => {
    if (USE_MOCK_DATA) {
      // Mock response
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            paymentId: 1,
            status: "pending",
          });
        }, 500);
      });
    }

    const response = await apiClient.post<CreatePaymentResponse>(
      "/payments",
      data,
    );
    return response.data;
  },

  /**
   * Get BPAY payment details by payment ID
   * GET /api/payments/:id
   * Returns BpayInfoResponse matching backend DTO
   */
  getPaymentDetails: async (paymentId: string): Promise<BpayInfoResponse> => {
    if (USE_MOCK_DATA) {
      // Mock response
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          const mockData = getMockBpayDetails(paymentId);
          if (mockData) {
            resolve(mockData);
          } else {
            reject(new Error(`Payment ${paymentId} not found`));
          }
        }, 800);
      });
    }

    const response = await apiClient.get<BpayInfoResponse>(
      `/payments/${paymentId}`,
    );
    return response.data;
  },

  /**
   * Request refund for a payment
   * POST /api/payments/:id/refund
   * Returns RefundResponse matching backend DTO
   */
  refundPayment: async (
    paymentId: string,
    reason: string,
  ): Promise<RefundResponse> => {
    if (USE_MOCK_DATA) {
      // Mock response
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            paymentId: Number(paymentId.replace(/\D/g, "")),
            status: "refunded",
            refundedAt: new Date().toISOString(),
          });
        }, 1000);
      });
    }

    const response = await apiClient.post<RefundResponse>(
      `/payments/${paymentId}/refund`,
      { reason },
    );
    return response.data;
  },
};
