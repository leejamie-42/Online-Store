import React from 'react';
import { formatCurrency } from '@/utils/formatters';
import type { PriceDisplayProps } from '@/types/product.types';

/**
 * PriceDisplay Component
 * Displays formatted price with currency symbol
 */
export const PriceDisplay: React.FC<PriceDisplayProps> = ({
  price,
  currency = 'USD',
  className = '',
}) => {
  const formattedPrice = formatCurrency(price, currency);

  return (
    <span className={`font-semibold text-gray-900 ${className}`}>
      {formattedPrice}
    </span>
  );
};
