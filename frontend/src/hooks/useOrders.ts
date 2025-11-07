import {
  useQuery,
  useMutation,
  useQueryClient,
  type UseQueryResult,
  type UseMutationResult,
} from "@tanstack/react-query";
import { orderService } from "@/api/services/order.service";
import type { Order, OrderHistoryResponse } from "@/types";

/**
 * Hook to fetch single order detail
 * @param orderId - Order ID (string)
 * @returns React Query result with order detail
 */
export const useOrder = (
  orderId: string | undefined
): UseQueryResult<Order, Error> => {
  return useQuery({
    queryKey: ["order", orderId],
    queryFn: () => {
      if (!orderId) {
        throw new Error("Order ID is required");
      }
      return orderService.getOrder(orderId);
    },
    enabled: !!orderId, // Only fetch if orderId exists
    staleTime: 1 * 60 * 1000, // 1 minute (orders update more frequently)
    retry: 2, // Retry failed requests twice
  });
};

/**
 * Hook to fetch user's order history
 * @returns React Query result with order history
 */
export const useOrders = (): UseQueryResult<OrderHistoryResponse[], Error> => {
  return useQuery({
    queryKey: ["orders"],
    queryFn: () => orderService.getUserOrders(),
    staleTime: 30 * 1000, // 30 seconds
    retry: 2,
  });
};

/**
 * Hook to cancel an order
 * Automatically invalidates order cache on success
 * @returns React Query mutation result for order cancellation
 */
export const useCancelOrder = (): UseMutationResult<
  Order,
  Error,
  string,
  unknown
> => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (orderId: string) => orderService.cancelOrder(orderId),
    onSuccess: (cancelledOrder, orderId) => {
      // Update the specific order in cache
      queryClient.setQueryData(["order", orderId], cancelledOrder);

      // Invalidate orders list to refetch with updated data
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });
};
