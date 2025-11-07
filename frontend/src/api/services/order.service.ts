/**
 * Order API Service
 * Handles all order-related API calls
 */

import { apiClient } from "@/lib/axios";
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  Order,
  OrderDetailResponse,
  OrderHistoryResponse,
} from "@/types";
import { API_ENDPOINTS } from "@/config/api.config";
import { convertIdWithPrifx } from "@/utils/formatters";
import { ID_PREFIX } from "@/utils/constants";
import { mockOrderHistory, mockOrder } from "@/mocks/order.mock";

/**
 * Check if we should use mock data
 */
const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === "true";

/**
 * Order API Service
 */
export const orderService = {
  /**
   * Create new order
   * POST /api/orders
   */
  createOrder: async (
    data: CreateOrderRequest,
  ): Promise<CreateOrderResponse> => {
    const response = await apiClient.post<CreateOrderResponse>(
      API_ENDPOINTS.ORDERS,
      data,
    );
    return response.data;
  },

  /**
   * Get order by ID
   * GET /api/orders/:id
   * Returns OrderDetailResponse matching backend DTO
   */
  getOrder: async (orderId: string): Promise<Order> => {
    if (USE_MOCK_DATA) {
      // Mock response with delay to simulate network
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve(mockOrder);
        }, 600);
      });
    }

    const response = await apiClient.get<OrderDetailResponse>(
      API_ENDPOINTS.ORDER_DETAIL(orderId),
    );
    return transformOrderDetailResponse(response.data);
  },

  /**
   * Get user's orders
   * GET /api/orders
   * Returns OrderSummaryResponse[] matching backend DTO
   */
  getUserOrders: async (): Promise<OrderHistoryResponse[]> => {
    if (USE_MOCK_DATA) {
      // Mock response
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve(mockOrderHistory);
        }, 600);
      });
    }

    const response = await apiClient.get<OrderHistoryResponse[]>(
      API_ENDPOINTS.ORDERS,
    );
    return response.data;
  },

  /**
   * Cancel order by ID
   * PUT /api/orders/:id/cancel
   * Returns updated order details with cancelled status
   */
  cancelOrder: async (orderId: string): Promise<Order> => {
    if (USE_MOCK_DATA) {
      // Mock cancellation - return mock order with cancelled status
      return new Promise((resolve) => {
        setTimeout(() => {
          console.log(`Order ${orderId} cancelled (mock)`);
          resolve({
            ...mockOrder,
            id: orderId,
            status: "cancelled",
            updatedAt: new Date().toISOString(),
          });
        }, 800);
      });
    }

    const response = await apiClient.put<OrderDetailResponse>(
      API_ENDPOINTS.CANCEL_ORDER(orderId),
    );
    return transformOrderDetailResponse(response.data);
  },
};

function transformOrderDetailResponse(order: OrderDetailResponse): Order {
  const {
    orderId,
    status,
    createdAt,
    updatedAt,
    shippingInfo,
    totalAmount,
    products,
  } = order;

  return {
    id: convertIdWithPrifx(ID_PREFIX.ORDER, orderId),
    status,
    shippingInfo,
    createdAt,
    updatedAt,
    total: totalAmount,
    products: products.map((product) => ({
      id: convertIdWithPrifx(ID_PREFIX.PRODUCT, product.id),
      name: product.name,
      price: product.price,
      quantity: product.quantity,
      imageUrl: product.imageUrl,
    })),
  };
}
