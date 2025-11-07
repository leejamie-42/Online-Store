/**
 * useBpayMutation Hook
 * Manages BPAY payment transfer completion
 *
 * Flow:
 * 1. Payment ID is obtained from URL params (created by useCheckout)
 * 2. Fetch BPAY details using usePayment hook
 * 3. User completes BPAY transfer (triggers Bank Service)
 * 4. Bank Service processes payment asynchronously
 * 5. Parent component polls for order status updates
 *
 * Note: This hook does NOT handle navigation - the parent component
 * should use useOrderStatusPolling to check order status and navigate
 * when payment is confirmed.
 */

import { useMutation } from "@tanstack/react-query";
import { bankService } from "@/api/services/bank.service";
import type {
  CompleteBpayTransferRequest,
  CompleteBpayTransferResponse,
} from "@/api/services/bank.service";
import type { BpayDetails } from "@/types";

export interface UseBpayPaymentReturn {
  // BPAY transfer completion
  completePaymentTransfer: (bpayDetails: BpayDetails, orderId: string) => void;
  isCompletingTransfer: boolean;
  transferError: Error | null;
  transferData: CompleteBpayTransferResponse | undefined;
}

/**
 * useBpayMutation Hook
 *
 * Handles BPAY payment transfer trigger to Bank Service.
 * BPAY details are fetched separately using usePayment hook.
 * Payment confirmation is handled via polling in parent component.
 *
 * @returns Payment transfer mutation and state
 */
export function useBpayMutation(): UseBpayPaymentReturn {
  /**
   * Mutation: Complete BPAY Transfer
   * Triggers Bank Service to process BPAY payment
   * Bank Service will webhook back to Store Backend asynchronously
   *
   * Note: This mutation completes immediately after triggering the bank
   * transfer. The parent component should poll order status to confirm
   * payment completion.
   */
  const transferMutation = useMutation<
    CompleteBpayTransferResponse,
    Error,
    CompleteBpayTransferRequest & { orderId: string }
  >({
    mutationFn: async (transferData) => {
      // Call Bank Service to simulate BPAY transfer
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { orderId, ...bankData } = transferData;
      return await bankService.completeBpayTransfer(bankData);
    },
    onSuccess: () => {
      // Transfer initiated successfully
      // Parent component handles navigation via polling
      console.log("BPAY transfer initiated successfully");
    },
    onError: (error) => {
      console.error("BPAY transfer error:", error);
    },
  });

  /**
   * Complete BPAY payment transfer
   * @param bpayDetails - BPAY payment details from GET /api/payments/{id}
   * @param orderId - Order ID for navigation after success
   */
  const completePaymentTransfer = (
    bpayDetails: BpayDetails,
    orderId: string,
  ) => {
    // Validate BPAY details
    if (!bpayDetails.billerCode || !bpayDetails.referenceNumber) {
      throw new Error("Invalid BPAY details");
    }

    // Create transfer request
    const transferData: CompleteBpayTransferRequest & { orderId: string } = {
      biller_code: bpayDetails.billerCode,
      reference_number: bpayDetails.referenceNumber,
      amount: bpayDetails.amount,
      orderId,
    };

    // Trigger transfer mutation
    transferMutation.mutate(transferData);
  };

  return {
    completePaymentTransfer,
    isCompletingTransfer: transferMutation.isPending,
    transferError: transferMutation.error,
    transferData: transferMutation.data,
  };
}
