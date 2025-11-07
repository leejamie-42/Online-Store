import React from 'react';
import type { Shipment } from '@/types/delivery.types';
import { ShipmentProgress } from '../ShipmentProgress';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';

interface ShipmentListProps {
  shipments: Shipment[];
  className?: string;
}

/**
 * ShipmentList Component
 * Lists all shipments for an order
 * When order comes from multiple warehouses, each one shows up separately
 */
export const ShipmentList: React.FC<ShipmentListProps> = ({
  shipments,
  className = '',
}) => {
  if (!shipments || shipments.length === 0) {
    return (
      <Card className={className}>
        <CardHeader>
          <h3 className="text-lg font-semibold">Shipments</h3>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-500">No shipments available yet</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <h3 className="text-lg font-semibold">Shipments</h3>
        <p className="text-sm text-gray-500 mt-1">
          {shipments.length} {shipments.length === 1 ? 'shipment' : 'shipments'} in progress
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {shipments.map((shipment) => (
            <ShipmentProgress key={shipment.id} shipment={shipment} />
          ))}
        </div>
      </CardContent>
    </Card>
  );
};
