/**
 * Bank Service API
 * Handles direct Bank Service interactions for payment simulation
 *
 * NOTE: In production, BPAY payments are processed through the user's banking app.
 * This service simulates the user completing a BPAY transfer for development/testing.
 */

import { apiClient } from "@/lib/axios";

export interface CompleteBpayTransferRequest {
  biller_code: string;
  reference_number: string;
  amount: number;
}

export interface CompleteBpayTransferResponse {
  transaction_id: string;
  status: "completed" | "pending" | "failed";
  completed_at: string;
}

/**
 * Bank Service API
 * Simulates BPAY payment transfer from user to merchant
 */
export const bankService = {
  /**
   * Simulate BPAY transfer completion
   * POST /bank/api/bpay/transfer
   *
   * This endpoint simulates the user completing a BPAY payment through their bank.
   * In production, this would be handled by the actual banking system and trigger
   * a webhook notification to the Store Backend.
   *
   * @param data - BPAY transfer details (biller code, reference, amount)
   * @returns Transaction confirmation
   */
  completeBpayTransfer: async (
    data: CompleteBpayTransferRequest,
  ): Promise<CompleteBpayTransferResponse> => {
    const response = await apiClient.post<CompleteBpayTransferResponse>(
      "/bank/bpay/transfer",
      data,
    );
    return response.data;
  },
};
