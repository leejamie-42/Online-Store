import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { Button } from '@/components/ui/Button/Button';

export interface NeedHelpSectionProps {
  onContactSupport?: () => void;
  className?: string;
}

export const NeedHelpSection: React.FC<NeedHelpSectionProps> = ({
  onContactSupport,
  className = '',
}) => {
  const handleContactSupport = () => {
    if (onContactSupport) {
      onContactSupport();
    } else {
      // Default behavior: open support email or chat
      window.location.href = 'mailto:support@shophub.com';
    }
  };

  return (
    <Card className={className}>
      <CardHeader>
        <h3 className="text-lg font-semibold text-gray-900">Need Help?</h3>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <Button
            variant="outline"
            className="w-full"
            onClick={handleContactSupport}
          >
            Contact Support
          </Button>
          <p className="text-sm text-gray-600 text-center">
            Available 24/7 to assist you
          </p>
        </div>
      </CardContent>
    </Card>
  );
};
