import React from 'react';
import type { StockBadgeProps } from '@/types/product.types';

/**
 * StockBadge Component
 * Displays stock availability status with color-coded badges
 *
 * Rules:
 * - stock > 10: Green "In Stock"
 * - stock 1-10: Yellow "Low Stock" with optional count
 * - stock = 0: Red "Out of Stock"
 */
export const StockBadge: React.FC<StockBadgeProps> = ({
  stock,
  showCount = false,
  className = '',
}) => {
  const getStockStatus = () => {
    if (stock === 0) {
      return {
        text: 'Out of Stock',
        classes: 'bg-red-100 text-red-800',
      };
    }

    if (stock <= 10) {
      const countText = showCount ? ` (${stock})` : '';
      return {
        text: `Low Stock${countText}`,
        classes: 'bg-yellow-100 text-yellow-800',
      };
    }

    return {
      text: 'In Stock',
      classes: 'bg-green-100 text-green-800',
    };
  };

  const { text, classes } = getStockStatus();

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${classes} ${className}`}
    >
      {text}
    </span>
  );
};
