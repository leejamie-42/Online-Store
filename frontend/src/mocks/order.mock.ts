import type { Order, OrderDetailResponse, OrderHistoryResponse } from "@/types";

export const mockOrderDetail: OrderDetailResponse = {
  orderId: 1,
  status: "delivered",
  userId: 1,
  totalAmount: 159.98,
  shippingInfo: {
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com",
    mobileNumber: "123-456-7890",
    addressLine1: "123 Main St",
    city: "Anytown",
    state: "CA",
    postcode: "12345",
    country: "USA",
  },
  products: [
    {
      id: 1,
      name: "Wireless Headphones",
      quantity: 2,
      price: 79.99,
      imageUrl:
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
    },
  ],
  createdAt: "2024-01-15T10:30:00Z",
  updatedAt: "2024-01-15T10:30:00Z",
};

/**
 * Mock order for OrderDetail page development
 * Simulates a complete order with multiple products
 */
export const mockOrder: Order = {
  id: "ORD-176060492528",
  status: "processing",
  total: 249.97,
  shippingInfo: {
    firstName: "Alice",
    lastName: "Johnson",
    email: "alice.johnson@example.com",
    mobileNumber: "+1 (555) 123-4567",
    addressLine1: "456 Oak Avenue",
    city: "San Francisco",
    state: "CA",
    postcode: "94102",
    country: "USA",
  },
  products: [
    {
      id: "PRD-001",
      name: "Wireless Bluetooth Headphones",
      quantity: 1,
      price: 149.99,
      imageUrl:
        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
    },
    {
      id: "PRD-002",
      name: "USB-C Charging Cable (3-Pack)",
      quantity: 1,
      price: 29.99,
      imageUrl:
        "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&q=80",
    },
    {
      id: "PRD-003",
      name: "Phone Stand Holder",
      quantity: 2,
      price: 34.99,
      imageUrl:
        "https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=500&q=80",
    },
  ],
  createdAt: "2025-10-16T19:55:00Z",
  updatedAt: "2025-10-18T14:30:00Z",
};

export const mockOrderHistory: OrderHistoryResponse[] = [
  {
    orderId: 1,
    createdAt: "2024-01-15T10:30:00Z",
    status: "delivered",
    totalAmount: 159.98,
    products: [
      {
        id: 1,
        name: "Wireless Headphones",
        quantity: 2,
        price: 79.99,
        imageUrl:
          "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
      },
    ],
  },
  {
    orderId: 2,
    createdAt: "2024-01-10T09:00:00Z",
    status: "cancelled",
    totalAmount: 89.99,
    products: [
      {
        id: 2,
        name: "Smart Watch",
        quantity: 1,
        price: 89.99,
        imageUrl:
          "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
      },
    ],
  },
  {
    orderId: 3,
    createdAt: "2024-01-05T14:20:00Z",
    status: "refunded",
    totalAmount: 199.99,
    products: [
      {
        id: 3,
        name: "Laptop Stand",
        quantity: 1,
        price: 199.99,
        imageUrl:
          "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&q=80",
      },
    ],
  },
];
