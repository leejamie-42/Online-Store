import type { EmailListResponse } from '@/types/email.types';

// Email service runs on port 8082
const EMAIL_API_URL = 'http://localhost:8082/api/emails';

/**
 * Gets all email notifications sent for an order
 * Returns them in chronological order
 */
export const getEmailHistory = async (orderId: number): Promise<EmailListResponse> => {
  try {
    const response = await fetch(`${EMAIL_API_URL}/order/${orderId}`);

    if (!response.ok) {
      throw new Error(`Failed to get email history: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching email history:', error);
    throw error;
  }
};
