import React from 'react';
import type { ProductSkeletonProps } from '@/types/product.types';

/**
 * ProductSkeleton Component
 * Loading skeleton for product cards
 */
export const ProductSkeleton: React.FC<ProductSkeletonProps> = ({
  className = '',
}) => {
  return (
    <div className={`animate-pulse ${className}`}>
      {/* Image placeholder */}
      <div className="bg-gray-300 aspect-square rounded-lg mb-4"></div>

      {/* Title placeholder */}
      <div className="h-4 bg-gray-200 rounded mb-2"></div>

      {/* Short text placeholder */}
      <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>

      {/* Price and badge row */}
      <div className="flex items-center justify-between mt-4">
        {/* Price placeholder */}
        <div className="h-5 bg-gray-200 rounded w-20"></div>

        {/* Badge placeholder */}
        <div className="h-5 bg-gray-200 rounded w-16"></div>
      </div>
    </div>
  );
};
