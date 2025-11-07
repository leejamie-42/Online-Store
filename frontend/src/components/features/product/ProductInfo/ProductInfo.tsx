import React from "react";
import { CategoryBadge } from "../CategoryBadge";
import type { ProductDetail } from "@/types/product.types";

interface ProductInfoProps {
  product: ProductDetail;
  category?: string;
}

/**
 * ProductInfo Component
 * Detailed product information display
 * Updated to match Figma design
 */
export const ProductInfo: React.FC<ProductInfoProps> = ({
  product,
  category = "Accessories",
}) => {
  return (
    <div className="space-y-[24px]">
      {/* Product Name and Category */}
      <div className="h-[56.5px] gap-[10.5px]">
        <h1
          className="
            text-[16px]
            font-normal
            leading-[24px]
            tracking-[-0.3125px]
            text-neutral-950
          "
        >
          {product.name}
        </h1>
        <CategoryBadge category={category} />
      </div>

      {/* Price */}
      <div className="h-[36px]">
        <p
          className="
            text-[30px]
            font-normal
            leading-[36px]
            tracking-[0.3955px]
            text-[#155dfc]
          "
        >
          ${product.price.toFixed(2)}
        </p>
      </div>

      {/* Description */}
      <div className="flex flex-col gap-[8px]">
        <h2
          className="
            text-[18px]
            font-medium
            leading-[27px]
            tracking-[-0.4395px]
            text-neutral-950
          "
        >
          Description
        </h2>
        <p
          className="
            text-[16px]
            font-normal
            leading-[24px]
            tracking-[-0.3125px]
            text-[#4a5565]
          "
        >
          {product.description}
        </p>
      </div>
    </div>
  );
};
