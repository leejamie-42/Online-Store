import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import type { ShippingInfo } from '@/types';

export interface OrderInfoPanelProps {
  orderId: string;
  orderDate: string;
  lastUpdated: string;
  shippingInfo: ShippingInfo;
  className?: string;
}

export const OrderInfoPanel: React.FC<OrderInfoPanelProps> = ({
  orderId,
  orderDate,
  lastUpdated,
  shippingInfo,
  className = '',
}) => {
  // Format date and time
  const formatDateTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  };

  // Format shipping address
  const formatAddress = (info: ShippingInfo): string => {
    return `${info.addressLine1}, ${info.city}, ${info.state} ${info.postcode}, ${info.country}`;
  };

  return (
    <Card className={className}>
      <CardHeader>
        <h2 className="text-lg font-semibold text-gray-900">Order Details</h2>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Order ID */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Order ID</p>
            <p className="text-base font-medium text-gray-900">{orderId}</p>
          </div>

          {/* Order Date */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Order Date</p>
            <p className="text-base text-gray-900">
              {formatDateTime(orderDate)}
            </p>
          </div>

          {/* Last Updated */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Last Updated</p>
            <p className="text-base text-gray-900">
              {formatDateTime(lastUpdated)}
            </p>
          </div>

          {/* Shipping Address */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Shipping Address</p>
            <p className="text-base text-gray-900">
              {formatAddress(shippingInfo)}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
