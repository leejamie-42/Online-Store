import React from 'react';
import type { Shipment } from '@/types/delivery.types';

interface ShipmentProgressProps {
  shipment: Shipment;
  className?: string;
}

/**
 * ShipmentProgress Component
 * Shows progress for a single shipment from a warehouse
 * Each shipment tracks independently so customers know where stuff is coming from
 */
export const ShipmentProgress: React.FC<ShipmentProgressProps> = ({
  shipment,
  className = '',
}) => {
  // Different colours for different shipment statuses
  const getStatusBadge = () => {
    switch (shipment.status) {
      case 'PENDING':
        return {
          text: 'Pending',
          classes: 'bg-gray-100 text-gray-800',
        };
      case 'PICKED_UP':
        return {
          text: 'Picked Up',
          classes: 'bg-yellow-100 text-yellow-800',
        };
      case 'IN_TRANSIT':
        return {
          text: 'In Transit',
          classes: 'bg-blue-100 text-blue-800',
        };
      case 'DELIVERED':
        return {
          text: 'Delivered',
          classes: 'bg-green-100 text-green-800',
        };
      case 'FAILED':
        return {
          text: 'Failed',
          classes: 'bg-red-100 text-red-800',
        };
      default:
        return {
          text: shipment.status,
          classes: 'bg-gray-100 text-gray-800',
        };
    }
  };

  const { text, classes } = getStatusBadge();

  return (
    <div className={`border border-gray-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <span className="text-sm font-medium text-gray-700">
            Warehouse #{shipment.warehouseId}
          </span>
          <span
            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${classes}`}
          >
            {text}
          </span>
        </div>
        <span className="text-sm text-gray-600">{shipment.progress}%</span>
      </div>

      {/* Progress bar */}
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div
          className="bg-blue-600 h-2 rounded-full transition-all duration-300"
          style={{ width: `${shipment.progress}%` }}
        />
      </div>

      {/* Updated timestamp */}
      <div className="mt-2 text-xs text-gray-500">
        Updated: {new Date(shipment.updatedAt).toLocaleString()}
      </div>
    </div>
  );
};
