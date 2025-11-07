/**
 * Order Type Definitions
 * Based on API spec: POST /api/orders and order management endpoints
 */

import type { Product, ProductDetail } from "./product.types";

export interface OrderProductResponse {
  id: number;
  name: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

/**
 * Order status represents the current state of an order
 * Transitions: pending → processing → picked_up → delivering → delivered
 * Can also transition to: cancelled → refunded
 */
export type OrderStatus =
  | "pending"
  | "processing"
  | "picked_up"
  | "delivering"
  | "delivered"
  | "cancelled"
  | "refunded";

/**
 * Shipping information required for order delivery
 * Matches the backend ShippingInfoDto.java (camelCase)
 * POST /api/orders → shippingInfo fields
 */
export interface ShippingInfo {
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  addressLine1: string;
  city: string;
  state: string;
  postcode: string;
  country: string;
}

/**
 * Individual item in an order
 * Captures product details and quantity at time of order
 */
export interface OrderItem {
  product: Product | ProductDetail;
  quantity: number;
  price: number; // Price at time of order (may differ from current product price)
}

/**
 * Order Detail Response
 * Matches backend OrderDetailResponse.java
 * Endpoint: GET /api/orders/{id}
 */
export interface OrderDetailResponse {
  orderId: number;
  userId: number;
  products: OrderProductResponse[];
  totalAmount: number;
  status: OrderStatus;
  shippingInfo: ShippingInfo;
  createdAt: string;
  updatedAt: string;
}

/**
 * Order Summary Response
 * Endpoint: GET /api/orders
 */
export interface OrderHistoryResponse {
  orderId: number;
  status: OrderStatus;
  products: OrderProductResponse[];
  totalAmount: number;
  createdAt: string; // ISO 8601 datetime
}

/**
 * Request payload for creating a new order
 * Matches POST /api/orders endpoint specification
 */
export interface CreateOrderRequest {
  productId: string;
  quantity: number;
  userId: number;
  shippingInfo: ShippingInfo;
}

/**
 * Response from order creation endpoint
 * POST /api/orders
 */
export interface CreateOrderResponse {
  orderId: number;
  status: OrderStatus;
  total: number;
}

type OrderProduct = {
  id: string;
  name: string;
  price: number;
  quantity: number;
  imageUrl: string;
};

export type Order = {
  id: string;
  products: OrderProduct[];
  status: OrderStatus;
  shippingInfo: ShippingInfo;
  total: number;
  createdAt: string;
  updatedAt: string;
};
