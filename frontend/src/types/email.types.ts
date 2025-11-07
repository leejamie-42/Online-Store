// Email notification types
// Based on EmailLogDto from Email Service

// Single email log entry
export interface EmailLog {
  id: number;
  orderId: number;
  emailType: string;
  recipient: string;
  subject: string;
  messageBody: string;
  sentAt: string;
  status: string;
  errorMessage?: string;
  retryCount?: number;
}

// Response from GET /api/emails/order/{orderId}
export type EmailListResponse = EmailLog[];
