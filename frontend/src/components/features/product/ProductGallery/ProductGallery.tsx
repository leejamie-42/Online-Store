import React, { useState } from 'react';

interface ProductGalleryProps {
  mainImage: string;
  altText: string;
  className?: string;
}

/**
 * ProductGallery Component
 * Image gallery for product detail page
 * Based on Figma design: node-id 1:196
 */
export const ProductGallery: React.FC<ProductGalleryProps> = ({
  mainImage,
  altText,
  className = '',
}) => {
  const [imageError, setImageError] = useState(false);

  if (!mainImage || imageError) {
    return (
      <div
        className={`
          aspect-square w-full
          bg-gray-200
          flex items-center justify-center
          rounded-xl
          ${className}
        `}
      >
        <span className="text-gray-500 text-lg">No Image</span>
      </div>
    );
  }

  return (
    <div className={`aspect-square w-full overflow-hidden rounded-xl ${className}`}>
      <img
        src={mainImage}
        alt={altText}
        onError={() => setImageError(true)}
        className="w-full h-full object-cover"
      />
    </div>
  );
};
