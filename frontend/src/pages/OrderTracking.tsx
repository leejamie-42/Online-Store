import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDeliveryStatus } from '@/api/services/delivery.service';
import { getEmailHistory } from '@/api/services/email.service';
import type { DeliveryStatusResponse } from '@/types/delivery.types';
import type { EmailListResponse } from '@/types/email.types';
import { ShipmentList } from '@/components/features/order/ShipmentList';
import { EmailHistoryList } from '@/components/features/order/EmailHistoryList';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { Spinner } from '@/components/ui/Spinner';

/**
 * OrderTracking Page
 * Shows live delivery status for an order with all shipments and email notifications
 * Updates automatically every 5 seconds so customers can watch their order progress
 */
const OrderTracking: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();

  const [deliveryStatus, setDeliveryStatus] = useState<DeliveryStatusResponse | null>(null);
  const [emails, setEmails] = useState<EmailListResponse>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date());

  // Fetch data from both APIs
  const fetchData = async () => {
    if (!orderId) return;

    try {
      setError(null);

      // Grab delivery status and emails at the same time
      const [deliveryData, emailData] = await Promise.all([
        getDeliveryStatus(Number(orderId)),
        getEmailHistory(Number(orderId)),
      ]);

      setDeliveryStatus(deliveryData);
      setEmails(emailData);
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Error fetching order tracking data:', err);
      setError('Failed to load order tracking information. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchData();
  }, [orderId]);

  // Auto-refresh every 5 seconds
  useEffect(() => {
    const interval = setInterval(() => {
      fetchData();
    }, 5000);

    return () => clearInterval(interval);
  }, [orderId]);

  // Overall delivery status badge
  const getOverallStatusBadge = () => {
    if (!deliveryStatus) return { text: 'Unknown', classes: 'bg-gray-100 text-gray-800' };

    const status = deliveryStatus.status;

    if (status === 'DELIVERED') {
      return { text: 'Delivered', classes: 'bg-green-100 text-green-800' };
    }
    if (status === 'IN_TRANSIT') {
      return { text: 'In Transit', classes: 'bg-blue-100 text-blue-800' };
    }
    if (status === 'PENDING') {
      return { text: 'Pending', classes: 'bg-yellow-100 text-yellow-800' };
    }
    if (status === 'FAILED') {
      return { text: 'Failed', classes: 'bg-red-100 text-red-800' };
    }

    return { text: status, classes: 'bg-gray-100 text-gray-800' };
  };

  if (!orderId) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <h3 className="text-lg font-semibold text-red-800 mb-2">Invalid Order</h3>
          <p className="text-red-600">No order ID provided</p>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <Spinner size="lg" />
        </div>
      </div>
    );
  }

  if (error || !deliveryStatus) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <h3 className="text-lg font-semibold text-red-800 mb-2">Error Loading Order</h3>
          <p className="text-red-600">{error || 'Order not found'}</p>
          <button
            onClick={() => navigate('/orders')}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  const statusBadge = getOverallStatusBadge();

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Page header */}
      <div className="mb-6">
        <div className="flex items-center gap-4 mb-2">
          <button
            onClick={() => navigate('/orders')}
            className="text-blue-600 hover:text-blue-700 text-sm"
          >
            ← Back to Orders
          </button>
        </div>
        <h1 className="text-3xl font-bold text-gray-900">Order Tracking</h1>
        <p className="text-gray-600 mt-2">Order #{orderId}</p>
      </div>

      {/* Auto-update indicator */}
      <div className="mb-4 text-sm text-gray-500 flex items-center gap-2">
        <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
        Auto-updating every 5 seconds • Last updated:{' '}
        {lastUpdated.toLocaleTimeString('en-AU')}
      </div>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column - delivery info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Overall delivery status */}
          <Card>
            <CardHeader>
              <h2 className="text-xl font-semibold">Delivery Status</h2>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-gray-700">Status</span>
                  <span
                    className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${statusBadge.classes}`}
                  >
                    {statusBadge.text}
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-gray-700">Overall Progress</span>
                  <span className="text-sm font-medium">{deliveryStatus.progress}%</span>
                </div>

                {/* Progress bar */}
                <div className="w-full bg-gray-200 rounded-full h-3">
                  <div
                    className="bg-blue-600 h-3 rounded-full transition-all duration-300"
                    style={{ width: `${deliveryStatus.progress}%` }}
                  />
                </div>

                <div className="pt-4 border-t border-gray-200 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Delivery to:</span>
                    <span className="font-medium">{deliveryStatus.customerName}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Email:</span>
                    <span className="font-medium">{deliveryStatus.customerEmail}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Address:</span>
                    <span className="font-medium text-right">{deliveryStatus.address}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Shipments list */}
          <ShipmentList shipments={deliveryStatus.shipments || []} />

          {/* Email notifications */}
          <EmailHistoryList emails={emails} />
        </div>

        {/* Right column - help/info */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <h3 className="text-lg font-semibold">Need Help?</h3>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-sm">
                <p className="text-gray-600">
                  If you have questions about your delivery, check your email notifications
                  or contact our support team.
                </p>
                <button className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                  Contact Support
                </button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h3 className="text-lg font-semibold">Delivery Info</h3>
            </CardHeader>
            <CardContent>
              <div className="space-y-2 text-sm text-gray-600">
                <p>• Orders are tracked in real-time</p>
                <p>• Multiple shipments may arrive separately</p>
                <p>• Email notifications sent for key updates</p>
                <p>• Delivery typically takes 3-5 business days</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default OrderTracking;
