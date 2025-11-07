import React from 'react';
import { Spinner } from '@/components/ui/Spinner';

interface PageLoaderProps {
  message?: string;
}

export const PageLoader: React.FC<PageLoaderProps> = ({ message = 'Loading...' }) => {
  return (
    <div className="min-h-[400px] flex flex-col items-center justify-center">
      <Spinner size="lg" />
      <p className="mt-4 text-gray-600 text-sm">{message}</p>
    </div>
  );
};
