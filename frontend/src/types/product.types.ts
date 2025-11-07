/**
 * Product Type Definitions
 * Aligned with backend ProductResponseDto
 * Backend source: com.comp5348.store.dto.ProductResponseDto
 */

// Base product interface (from GET /api/products)
export interface ProductResponse {
  id: number; // Changed from string to number to match backend Long
  name: string;
  price: number;
  stock: number;
  imageUrl: string; // Changed from image_url to imageUrl (camelCase)
  published: boolean;
}

// Extended product interface (from GET /api/products/{id})
export interface ProductDetailResponse extends ProductResponse {
  description: string;
}

// Product filters for API query params
export interface ProductFilters {
  published?: boolean;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  inStock?: boolean;
}

// Product sort options
export type ProductSortField = "name" | "price" | "stock";
export type ProductSortOrder = "asc" | "desc";

export interface ProductSort {
  field: ProductSortField;
  order: ProductSortOrder;
}

// API Response types
export interface ProductListResponse {
  products: ProductResponse[];
  total: number;
}

// Component props types
export interface ProductCardProps {
  product: ProductResponse;
  onClick?: (product: ProductResponse) => void;
  className?: string;
}

export interface ProductImageProps {
  src: string;
  alt: string;
  className?: string;
  imageClassName?: string;
}

export interface PriceDisplayProps {
  price: number;
  currency?: string;
  className?: string;
}

export interface StockBadgeProps {
  stock: number;
  showCount?: boolean;
  className?: string;
}

export interface ProductSkeletonProps {
  className?: string;
}

export type Product = {
  id: string;
  name: string;
  price: number;
  stock: number;
  imageUrl: string; // Changed from image_url to imageUrl (camelCase)
  published: boolean;
};

export type ProductDetail = Product & {
  description: string;
};
