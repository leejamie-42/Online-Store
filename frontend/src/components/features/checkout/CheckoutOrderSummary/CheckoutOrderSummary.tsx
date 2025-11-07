/**
 * CheckoutOrderSummary Component
 * Sticky sidebar showing order summary during checkout
 * Based on Figma design: Customer Details Page - Order Summary sidebar
 */

import React from 'react';
import { Card } from '@/components/ui/Card/Card';
import type { CartSummary } from '@/types/cart.types';

export interface CheckoutOrderSummaryProps {
  summary: CartSummary;
  className?: string;
}

/**
 * CheckoutOrderSummary Component
 * Displays order pricing breakdown in a sticky sidebar
 * Reusable across checkout steps (details, payment, confirmation)
 *
 * @param summary - Cart summary with pricing details
 * @param className - Optional additional CSS classes
 */
export const CheckoutOrderSummary: React.FC<CheckoutOrderSummaryProps> = ({
  summary,
  className = '',
}) => {
  return (
    <div className={className}>
      <Card padding="lg" className="sticky top-4">
        <h3 className="text-xl font-semibold text-gray-900 mb-6">
          Order Summary
        </h3>

        <div className="space-y-4">
          {/* Items Count */}
          <div className="flex justify-between items-center text-sm">
            <span className="text-gray-600">
              Items ({summary.itemCount})
            </span>
            <span className="font-medium text-gray-900">
              ${summary.subtotal.toFixed(2)}
            </span>
          </div>

          {/* Shipping */}
          <div className="flex justify-between items-center text-sm">
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
          <div className="flex justify-between items-center text-sm">
            <span className="text-gray-600">Tax (10% GST)</span>
            <span className="font-medium text-gray-900">
              ${summary.tax.toFixed(2)}
            </span>
          </div>

          {/* Divider */}
          <div className="border-t border-gray-200 pt-4" />

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

        {/* Free Shipping Message */}
        {summary.shipping === 0 && summary.subtotal >= 100 && (
          <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg">
            <p className="text-xs text-green-800 text-center">
              🎉 You qualified for free shipping!
            </p>
          </div>
        )}

        {/* Almost Free Shipping Message */}
        {summary.shipping > 0 && summary.subtotal < 100 && (
          <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <p className="text-xs text-blue-800 text-center">
              Add ${(100 - summary.subtotal).toFixed(2)} more for free shipping
            </p>
          </div>
        )}
      </Card>
    </div>
  );
};
