/**
 * OrderTotal Component
 * Displays pricing breakdown with subtotal, shipping, tax, and total
 * Based on Figma design: Order total sidebar in checkout
 */

import React from 'react';
import { Card } from '@/components/ui/Card/Card';
import { Alert, AlertDescription } from '@/components/ui/Alert/Alert';
import type { CartSummary } from '@/types/cart.types';

export interface OrderTotalProps {
  summary: CartSummary;
  className?: string;
}

/**
 * OrderTotal Component
 * Displays complete pricing breakdown for the order
 *
 * @param summary - Cart summary with pricing details
 * @param className - Optional additional CSS classes
 */
export const OrderTotal: React.FC<OrderTotalProps> = ({
  summary,
  className = '',
}) => {
  return (
    <div className={className}>
      <Card padding="lg" className="sticky top-4">
        <h3 className="text-xl font-semibold text-gray-900 mb-4">
          Order Total
        </h3>

        <div className="space-y-3">
          {/* Subtotal */}
          <div className="flex justify-between items-center">
            <span className="text-gray-600">Subtotal</span>
            <span className="font-medium text-gray-900">
              ${summary.subtotal.toFixed(2)}
            </span>
          </div>

          {/* Shipping */}
          <div className="flex justify-between items-center">
            <span className="text-gray-600">Shipping</span>
            <span className="font-medium text-gray-900">
              {summary.shipping === 0 ? (
                <span className="text-green-600 font-semibold">FREE</span>
              ) : (
                `$${summary.shipping.toFixed(2)}`
              )}
            </span>
          </div>

          {/* Tax */}
          <div className="flex justify-between items-center">
            <span className="text-gray-600">Tax (8%)</span>
            <span className="font-medium text-gray-900">
              ${summary.tax.toFixed(2)}
            </span>
          </div>

          {/* Divider */}
          <div className="border-t border-gray-200 my-3" />

          {/* Total */}
          <div className="flex justify-between items-center">
            <span className="text-lg font-semibold text-gray-900">
              Total
            </span>
            <span className="text-lg font-bold text-primary-600">
              ${summary.total.toFixed(2)}
            </span>
          </div>
        </div>

        {/* Info Alert */}
        <Alert variant="info" className="mt-4">
          <AlertDescription>
            In the next step, you'll provide your shipping details and complete payment via BPAY.
          </AlertDescription>
        </Alert>

        {/* Free Shipping Message */}
        {summary.shipping === 0 && summary.subtotal >= 100 && (
          <div className="mt-3 text-xs text-green-600 text-center">
            🎉 You qualified for free shipping!
          </div>
        )}

        {/* Almost Free Shipping Message */}
        {summary.shipping > 0 && summary.subtotal < 100 && (
          <div className="mt-3 text-xs text-gray-600 text-center">
            Add ${(100 - summary.subtotal).toFixed(2)} more for free shipping
          </div>
        )}
      </Card>
    </div>
  );
};
