import React from 'react';

interface CategoryBadgeProps {
  category: string;
  className?: string;
}

/**
 * CategoryBadge Component
 * Display product category badge
 * Based on Figma design: node-id 1:211
 */
export const CategoryBadge: React.FC<CategoryBadgeProps> = ({
  category,
  className = '',
}) => {
  return (
    <span
      className={`
        inline-flex items-center
        px-[9px] py-[4px]
        bg-[#eceef2]
        text-[#030213]
        text-[12px]
        font-medium
        leading-[16px]
        tracking-[-0.1504px]
        rounded-[8px]
        h-[22px]
        ${className}
      `}
    >
      {category}
    </span>
  );
};
