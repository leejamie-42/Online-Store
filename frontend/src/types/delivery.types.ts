// Types for delivery tracking
// Matches the backend DeliveryStatusDto from DeliveryCo service

// Individual shipment from a warehouse
export interface Shipment {
  id: number;
  deliveryRequestId?: number;
  warehouseId: number;
  status: string;
  progress: number;
  createdAt: string;
  updatedAt: string;
}

// Response from GET /api/deliveries/{orderId}/status
// This is what DeliveryStatusDto sends back
export interface DeliveryStatusResponse {
  id: number;
  orderId: number;
  status: string;
  progress: number;
  customerName: string;
  customerEmail: string;
  address: string;
  shipments: Shipment[];
  createdAt: string;
  updatedAt: string;
}
