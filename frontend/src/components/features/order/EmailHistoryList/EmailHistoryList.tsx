import React from 'react';
import type { EmailLog } from '@/types/email.types';
import { EmailHistoryItem } from '../EmailHistoryItem';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';

interface EmailHistoryListProps {
  emails: EmailLog[];
  className?: string;
}

/**
 * EmailHistoryList Component
 * Shows all email notifications sent for an order in timeline format
 * Most recent emails show at the top
 */
export const EmailHistoryList: React.FC<EmailHistoryListProps> = ({
  emails,
  className = '',
}) => {
  if (!emails || emails.length === 0) {
    return (
      <Card className={className}>
        <CardHeader>
          <h3 className="text-lg font-semibold">Email Notifications</h3>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-500">No email notifications yet</p>
        </CardContent>
      </Card>
    );
  }

  // Sort by sent time, newest first
  const sortedEmails = [...emails].sort((a, b) => {
    return new Date(b.sentAt).getTime() - new Date(a.sentAt).getTime();
  });

  return (
    <Card className={className}>
      <CardHeader>
        <h3 className="text-lg font-semibold">Email Notifications</h3>
        <p className="text-sm text-gray-500 mt-1">
          {emails.length} {emails.length === 1 ? 'notification' : 'notifications'} sent
        </p>
      </CardHeader>
      <CardContent>
        <div className="space-y-0">
          {sortedEmails.map((email) => (
            <EmailHistoryItem key={email.id} email={email} />
          ))}
        </div>
      </CardContent>
    </Card>
  );
};
