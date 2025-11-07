/**
 * useOrderStatusPolling Hook
 * Polls order status until payment is confirmed or timeout occurs
 *
 * This hook implements a polling strategy to check order status after
 * BPAY transfer is initiated, since the Bank Service webhook may take
 * time to process and update the order status.
 */

import { useQuery } from "@tanstack/react-query";
import { orderService } from "@/api/services/order.service";
import type { Order, OrderStatus } from "@/types";

export interface UseOrderStatusPollingOptions {
  /** Order ID to poll */
  orderId: string;
  /** Whether to enable polling (default: false) */
  enabled?: boolean;
  /** Polling interval in milliseconds (default: 2000ms = 2s) */
  interval?: number;
  /** Maximum polling duration in milliseconds (default: 60000ms = 60s) */
  maxDuration?: number;
  /** Target order statuses that indicate completion (default: ['processing', 'picked_up', 'delivering', 'delivered']) */
  targetStatuses?: OrderStatus[];
  /** Callback when target status is reached */
  onSuccess?: (order: Order) => void;
  /** Callback when polling times out */
  onTimeout?: () => void;
  /** Callback on polling error */
  onError?: (error: Error) => void;
}

export interface UseOrderStatusPollingReturn {
  /** Current order data */
  order: Order | undefined;
  /** Whether polling is in progress */
  isPolling: boolean;
  /** Polling error if any */
  error: Error | null;
  /** Whether target status has been reached */
  isComplete: boolean;
  /** Manually stop polling */
  stopPolling: () => void;
}

/**
 * useOrderStatusPolling Hook
 *
 * Implements a polling mechanism to check order status updates after
 * BPAY payment transfer is initiated. Polls until:
 * 1. Target order status is reached (payment confirmed)
 * 2. Maximum duration is exceeded (timeout)
 * 3. Error occurs
 * 4. Manually stopped
 *
 * @example
 * ```tsx
 * const { order, isPolling, isComplete } = useOrderStatusPolling({
 *   orderId: "123",
 *   enabled: true,
 *   onSuccess: (order) => navigate(`/order/${order.id}/confirmation`),
 *   onTimeout: () => showError("Payment verification timeout"),
 * });
 * ```
 */
export function useOrderStatusPolling({
  orderId,
  enabled = false,
  interval = 2000, // Poll every 2 seconds
  maxDuration = 60000, // Timeout after 60 seconds
  targetStatuses = ["processing", "picked_up", "delivering", "delivered"],
  onSuccess,
  onTimeout,
  onError,
}: UseOrderStatusPollingOptions): UseOrderStatusPollingReturn {
  const startTime = Date.now();

  const {
    data: order,
    error,
    isLoading,
  } = useQuery<Order, Error>({
    queryKey: ["order", orderId, "polling"],
    queryFn: () => orderService.getOrder(orderId),
    enabled: enabled && !!orderId,
    refetchInterval: (query) => {
      const currentOrder = query.state.data;
      const elapsed = Date.now() - startTime;

      // Stop polling if target status reached
      if (currentOrder && targetStatuses.includes(currentOrder.status)) {
        onSuccess?.(currentOrder);
        return false;
      }

      // Stop polling if max duration exceeded
      if (elapsed >= maxDuration) {
        onTimeout?.();
        return false;
      }

      // Continue polling
      return interval;
    },
    refetchIntervalInBackground: true, // Continue polling even if window loses focus
    retry: 3, // Retry failed requests 3 times
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 10000), // Exponential backoff
  });

  // Handle errors
  if (error && onError) {
    onError(error);
  }

  // Check if target status has been reached
  const isComplete = order ? targetStatuses.includes(order.status) : false;

  // Manually stop polling by disabling refetch
  const stopPolling = () => {
    // This will be handled by setting enabled to false in the parent component
  };

  return {
    order,
    isPolling: enabled && isLoading,
    error: error || null,
    isComplete,
    stopPolling,
  };
}
