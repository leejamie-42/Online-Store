/**
 * Request payload for creating a payment
 * POST /api/payments
 */
export interface CreatePaymentRequest {
  orderId: number;
  method: string; // "BPAY"
}

/**
 * Response from payment creation endpoint
 * POST /api/payments
 */
export interface CreatePaymentResponse {
  paymentId: number;
  status: string; // "pending", "completed", "refunded"
}

/**
 * BPAY Info Response
 * Endpoint: GET /api/payments/{id}
 */
export interface BpayInfoResponse {
  billerCode: string;
  referenceNumber: string;
  amount: number;
  expiresAt: string;
}

export type BpayDetails = BpayInfoResponse;

export interface RefundRequest {
  reason: string;
}

/**
 * Refund Response
 * Endpoint: POST /api/payments/{id}/refund
 */
export interface RefundResponse {
  paymentId: number;
  status: string;
  refundedAt: string | null; // null if refund is still processing
}
