import React from 'react';
import type { EmailLog } from '@/types/email.types';

interface EmailHistoryItemProps {
  email: EmailLog;
  className?: string;
}

/**
 * EmailHistoryItem Component
 * Shows a single email notification in the timeline
 * Displays email subject, preview, and sent time
 */
export const EmailHistoryItem: React.FC<EmailHistoryItemProps> = ({
  email,
  className = '',
}) => {
  // Email type colours - order confirmation is blue, delivery updates green, etc
  const getEmailTypeBadge = () => {
    const type = email.emailType.toUpperCase();

    if (type.includes('CONFIRMATION') || type.includes('ORDER')) {
      return 'bg-blue-100 text-blue-800';
    }
    if (type.includes('DELIVERY') || type.includes('SHIPPED')) {
      return 'bg-green-100 text-green-800';
    }
    if (type.includes('PAYMENT')) {
      return 'bg-purple-100 text-purple-800';
    }

    return 'bg-gray-100 text-gray-800';
  };

  // Status badge for sent/failed emails
  const getStatusBadge = () => {
    if (email.status === 'SENT') {
      return {
        text: 'Sent',
        classes: 'bg-green-100 text-green-800',
      };
    }

    if (email.status === 'FAILED') {
      return {
        text: 'Failed',
        classes: 'bg-red-100 text-red-800',
      };
    }

    return {
      text: email.status,
      classes: 'bg-gray-100 text-gray-800',
    };
  };

  const statusBadge = getStatusBadge();

  // Format the date nicely
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('en-AU', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className={`border-l-2 border-gray-200 pl-4 pb-4 ${className}`}>
      <div className="flex items-start gap-3">
        {/* Timeline dot */}
        <div className="flex-shrink-0 w-3 h-3 rounded-full bg-blue-500 -ml-[1.6rem] mt-1.5" />

        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1">
            <span
              className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${getEmailTypeBadge()}`}
            >
              {email.emailType}
            </span>
            <span
              className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${statusBadge.classes}`}
            >
              {statusBadge.text}
            </span>
          </div>

          <h4 className="text-sm font-semibold text-gray-900 mb-1">
            {email.subject}
          </h4>

          <p className="text-sm text-gray-600 mb-2 line-clamp-2">
            {email.messageBody}
          </p>

          <div className="flex items-center gap-3 text-xs text-gray-500">
            <span>To: {email.recipient}</span>
            <span>•</span>
            <span>{formatDate(email.sentAt)}</span>
          </div>

          {/* Show error message if email failed */}
          {email.status === 'FAILED' && email.errorMessage && (
            <div className="mt-2 text-xs text-red-600 bg-red-50 px-2 py-1 rounded">
              Error: {email.errorMessage}
            </div>
          )}

          {/* Show retry count if there were retries */}
          {email.retryCount && email.retryCount > 0 && (
            <div className="mt-2 text-xs text-gray-500">
              Retried {email.retryCount} {email.retryCount === 1 ? 'time' : 'times'}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
