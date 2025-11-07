import type { DeliveryStatusResponse } from '@/types/delivery.types';

// DeliveryCo service runs on port 8081
const DELIVERY_API_URL = 'http://localhost:8081/api/deliveries';

/**
 * Gets the current delivery status for an order
 * Includes all shipments from different warehouses
 */
export const getDeliveryStatus = async (orderId: number): Promise<DeliveryStatusResponse> => {
  try {
    const response = await fetch(`${DELIVERY_API_URL}/${orderId}/status`);

    if (!response.ok) {
      throw new Error(`Failed to get delivery status: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching delivery status:', error);
    throw error;
  }
};
