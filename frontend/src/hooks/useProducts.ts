import { useQuery, type UseQueryResult } from "@tanstack/react-query";
import { productService } from "@/api/services/product.service";
import type {
  Product,
  ProductDetail,
  ProductFilters,
} from "@/types/product.types";

/**
 * Hook to fetch list of products
 * @param filters - Optional filters for products
 * @returns React Query result with products data
 */
export const useProducts = (
  filters?: ProductFilters,
): UseQueryResult<Product[], Error> => {
  return useQuery({
    queryKey: ["products", filters],
    queryFn: () => productService.getProducts(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook to fetch single product detail
 * @param productId - Product ID (number)
 * @returns React Query result with product detail
 */
export const useProduct = (
  productId: string | undefined,
): UseQueryResult<ProductDetail, Error> => {
  return useQuery({
    queryKey: ["product", productId],
    queryFn: () => {
      if (!productId) {
        throw new Error("Product ID is required");
      }
      return productService.getProduct(productId);
    },
    enabled: !!productId, // Only fetch if productId exists
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
