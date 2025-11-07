import React from "react";
import { Card } from "@/components/ui/Card";
import { ProductImage } from "../ProductImage";
import { PriceDisplay } from "../PriceDisplay";
import { StockBadge } from "../StockBadge";
import type { ProductCardProps } from "@/types/product.types";

/**
 * ProductCard Component
 * Displays product information in a card format
 * Composes ProductImage, PriceDisplay, and StockBadge
 */
export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onClick,
  className = "",
}) => {
  const handleClick = () => {
    if (onClick) {
      onClick(product);
    }
  };

  const isClickable = !!onClick;

  return (
    <div
      className={`h-fit overflow-hidden transition-shadow ${
        isClickable ? "cursor-pointer hover:shadow-lg" : ""
      } ${className}`}
      onClick={isClickable ? handleClick : undefined}
    >
      <Card padding="none">
        {/* Product Image */}
        <ProductImage
          src={product.imageUrl}
          alt={product.name}
          className="aspect-square"
        />

        {/* Product Info */}
        <div className="p-4">
          {/* Product Name */}
          <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
            {product.name}
          </h3>

          {/* Price and Stock Badge */}
          <div className="flex items-center justify-between mt-4">
            <PriceDisplay price={product.price} />
            <StockBadge stock={product.stock} />
          </div>
        </div>
      </Card>
    </div>
  );
};
