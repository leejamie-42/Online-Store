import React, { useState } from 'react';
import type { ProductImageProps } from '@/types/product.types';

/**
 * ProductImage Component
 * Displays product images with fallback handling and loading states
 */
export const ProductImage: React.FC<ProductImageProps> = ({
  src,
  alt,
  className = '',
  imageClassName = '',
}) => {
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  const handleLoad = () => {
    setIsLoading(false);
  };

  const handleError = () => {
    setHasError(true);
    setIsLoading(false);
  };

  const imageSrc = hasError ? '/placeholder-product.png' : src;

  return (
    <div
      className={`relative overflow-hidden ${
        isLoading ? 'bg-gray-200 animate-pulse' : ''
      } ${className}`}
    >
      <img
        src={imageSrc}
        alt={alt}
        onLoad={handleLoad}
        onError={handleError}
        className={`w-full h-full object-cover ${imageClassName}`}
      />
    </div>
  );
};
