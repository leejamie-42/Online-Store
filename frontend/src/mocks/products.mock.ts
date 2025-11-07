import type {
  ProductResponse,
  ProductDetailResponse,
} from "@/types/product.types";

/**
 * Mock product data for development and testing
 * Used when backend API is not available
 */

export const mockProducts: ProductResponse[] = [
  {
    id: 1,
    name: "Wireless Bluetooth Headphones",
    price: 79.99,
    stock: 45,
    imageUrl:
      "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
    published: true,
  },
  {
    id: 2,
    name: "Mechanical Gaming Keyboard",
    price: 129.99,
    stock: 8,
    imageUrl:
      "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
    published: true,
  },
  {
    id: 3,
    name: "Ergonomic Wireless Mouse",
    price: 49.99,
    stock: 0,
    imageUrl:
      "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&q=80",
    published: true,
  },
  {
    id: 4,
    name: "USB-C Charging Cable (2m)",
    price: 19.99,
    stock: 150,
    imageUrl:
      "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&q=80",
    published: true,
  },
  {
    id: 5,
    name: "4K Webcam with Microphone",
    price: 159.99,
    stock: 23,
    imageUrl:
      "https://images.unsplash.com/photo-1587826080692-f439cd0b70da?w=500&q=80",
    published: true,
  },
  {
    id: 6,
    name: "Laptop Stand Adjustable",
    price: 39.99,
    stock: 5,
    imageUrl:
      "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&q=80",
    published: true,
  },
  {
    id: 7,
    name: "Portable SSD 1TB",
    price: 199.99,
    stock: 67,
    imageUrl:
      "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=500&q=80",
    published: true,
  },
  {
    id: 8,
    name: "Smart Watch Fitness Tracker",
    price: 249.99,
    stock: 12,
    imageUrl:
      "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
    published: true,
  },
  {
    id: 9,
    name: "Wireless Phone Charger",
    price: 34.99,
    stock: 89,
    imageUrl:
      "https://images.unsplash.com/photo-1591290619762-c588081f5b86?w=500&q=80",
    published: true,
  },
  {
    id: 10,
    name: "Noise Cancelling Earbuds",
    price: 149.99,
    stock: 3,
    imageUrl:
      "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
    published: true,
  },
  {
    id: 11,
    name: "USB Hub 7-Port",
    price: 29.99,
    stock: 0,
    imageUrl:
      "https://images.unsplash.com/photo-1625948515291-69613efd103f?w=500&q=80",
    published: true,
  },
  {
    id: 12,
    name: "LED Desk Lamp with USB",
    price: 44.99,
    stock: 56,
    imageUrl:
      "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&q=80",
    published: true,
  },
];

export const mockProductDetails: Record<number, ProductDetailResponse> = {
  1: {
    id: 1,
    name: "Wireless Bluetooth Headphones",
    description:
      "Premium wireless headphones with active noise cancellation, 30-hour battery life, and superior sound quality. Perfect for music lovers and professionals.",
    price: 79.99,
    stock: 45,
    imageUrl:
      "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
    published: true,
  },
  2: {
    id: 2,
    name: "Mechanical Gaming Keyboard",
    description:
      "RGB backlit mechanical keyboard with customizable keys, anti-ghosting technology, and durable construction. Ideal for gamers and typists.",
    price: 129.99,
    stock: 8,
    imageUrl:
      "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
    published: true,
  },
  3: {
    id: 3,
    name: "Ergonomic Wireless Mouse",
    description:
      "Ergonomically designed wireless mouse with precision tracking and comfortable grip. Reduces wrist strain during extended use.",
    price: 49.99,
    stock: 0,
    imageUrl:
      "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&q=80",
    published: true,
  },
};

/**
 * Filter mock products based on criteria
 */
export const filterMockProducts = (
  products: ProductResponse[],
  filters?: {
    published?: boolean;
    minPrice?: number;
    maxPrice?: number;
    search?: string;
    inStock?: boolean;
  },
): ProductResponse[] => {
  if (!filters) return products;

  return products.filter((product) => {
    if (
      filters.published !== undefined &&
      product.published !== filters.published
    ) {
      return false;
    }

    if (filters.minPrice !== undefined && product.price < filters.minPrice) {
      return false;
    }

    if (filters.maxPrice !== undefined && product.price > filters.maxPrice) {
      return false;
    }

    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      if (!product.name.toLowerCase().includes(searchLower)) {
        return false;
      }
    }

    if (filters.inStock !== undefined) {
      const hasStock = product.stock > 0;
      if (filters.inStock !== hasStock) {
        return false;
      }
    }

    return true;
  });
};
