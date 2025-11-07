/**
 * Mock Data for BPAY Payment Page
 * Provides realistic test data for development and testing
 */

import type { BpayDetails } from "@/types";

/**
 * Mock BPAY Details
 * Simulates response from GET /api/payments/:id
 */
export const mockBpayDetails: Record<string, BpayDetails> = {
  "PMT-001": {
    billerCode: "123456",
    referenceNumber: "9876543210",
    amount: 299.99,
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 24 hours from now
  },
  "PMT-EXPIRED": {
    billerCode: "999999",
    referenceNumber: "0000000000",
    amount: 99.99,
    expiresAt: new Date(Date.now() - 1000).toISOString(), // Expired 1 second ago
  },
  "PMT-LARGE": {
    billerCode: "111222",
    referenceNumber: "8888888888",
    amount: 1234.56,
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  },
};

/**
 * Mock Payment to Order Mapping
 * Maps payment IDs to order IDs for testing
 */
export const mockPaymentOrderMapping: Record<string, string> = {
  "PMT-001": "ORD-001",
  "PMT-002": "ORD-002",
  "PMT-003": "ORD-003",
  "PMT-EXPIRED": "ORD-001",
  "PMT-LARGE": "ORD-003",
};

/**
 * Get mock BPAY details by payment ID
 */
export function getMockBpayDetails(paymentId: string): BpayDetails | null {
  return mockBpayDetails[paymentId] || null;
}

/**
 * Generate random BPAY details for testing
 */
export function generateRandomBpayDetails(amount: number): BpayDetails {
  const randomBillerCode = Math.floor(
    100000 + Math.random() * 900000,
  ).toString();
  const randomReference = Math.floor(
    1000000000 + Math.random() * 9000000000,
  ).toString();

  return {
    billerCode: randomBillerCode,
    referenceNumber: randomReference,
    amount,
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  };
}

/**
 * Mock BPAY Details for different scenarios
 */
export const mockBpayScenarios = {
  // Standard payment
  standard: mockBpayDetails["PMT-001"],

  // Small amount
  small: {
    billerCode: "100000",
    referenceNumber: "1111111111",
    amount: 9.99,
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  },

  // Large amount
  large: mockBpayDetails["PMT-LARGE"],

  // Expired payment
  expired: mockBpayDetails["PMT-EXPIRED"],

  // About to expire (1 hour remaining)
  expiringSoon: {
    billerCode: "200000",
    referenceNumber: "2222222222",
    amount: 199.99,
    expiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString(), // 1 hour
  },

  // Exact amount (no decimals)
  exactAmount: {
    billerCode: "300000",
    referenceNumber: "3333333333",
    amount: 500.0,
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
  },
};

/**
 * Helper function to check if payment is expired
 */
export function isPaymentExpired(bpayDetails: BpayDetails): boolean {
  return new Date(bpayDetails.expiresAt) < new Date();
}

/**
 * Helper function to get time remaining until expiry
 */
export function getTimeRemaining(bpayDetails: BpayDetails): {
  hours: number;
  minutes: number;
  seconds: number;
  isExpired: boolean;
} {
  const now = new Date().getTime();
  const expiry = new Date(bpayDetails.expiresAt).getTime();
  const diff = expiry - now;

  if (diff <= 0) {
    return { hours: 0, minutes: 0, seconds: 0, isExpired: true };
  }

  const hours = Math.floor(diff / (1000 * 60 * 60));
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diff % (1000 * 60)) / 1000);

  return { hours, minutes, seconds, isExpired: false };
}

/**
 * Format expiry time as human-readable string
 */
export function formatExpiryTime(bpayDetails: BpayDetails): string {
  const { hours, minutes, isExpired } = getTimeRemaining(bpayDetails);

  if (isExpired) {
    return "Expired";
  }

  if (hours > 0) {
    return `${hours}h ${minutes}m remaining`;
  }

  return `${minutes}m remaining`;
}
