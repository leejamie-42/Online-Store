/**
 * usePayment Hook
 * Fetches BPAY payment details by payment ID using React Query
 */

import { useQuery } from "@tanstack/react-query";
import { paymentService } from "@/api/services/payment.service";
import type { BpayDetails } from "@/types/order.types";

export interface UsePaymentReturn {
  bpayDetails: BpayDetails | undefined;
  isLoading: boolean;
  error: Error | null;
  refetch: () => void;
}

/**
 * usePayment Hook
 *
 * Fetches BPAY payment details from GET /api/payments/:id
 *
 * @param paymentId - The payment ID from the URL
 */
export function usePayment(paymentId: string | undefined): UsePaymentReturn {
  const {
    data: bpayDetails,
    isLoading,
    error,
    refetch,
  } = useQuery<BpayDetails, Error>({
    queryKey: ["payment", paymentId],
    queryFn: () => {
      if (!paymentId) {
        throw new Error("Payment ID is required");
      }
      return paymentService.getPaymentDetails(paymentId);
    },
    enabled: !!paymentId, // Only run query if paymentId exists
    staleTime: 5 * 60 * 1000, // Consider data fresh for 5 minutes
    gcTime: 10 * 60 * 1000, // Keep in cache for 10 minutes (formerly cacheTime)
    retry: 3, // Retry failed requests 3 times
  });

  return {
    bpayDetails,
    isLoading,
    error,
    refetch,
  };
}
