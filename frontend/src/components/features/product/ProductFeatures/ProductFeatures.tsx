import React from 'react';

interface ProductFeaturesProps {
  features?: string[];
  className?: string;
}

const defaultFeatures = [
  'Secure payment processing',
  'Free shipping on orders over $100',
  '30-day return policy',
];

/**
 * ProductFeatures Component
 * Display product features/benefits with checkmarks
 * Based on Figma design: node-id 1:243
 */
export const ProductFeatures: React.FC<ProductFeaturesProps> = ({
  features = defaultFeatures,
  className = '',
}) => {
  if (features.length === 0) return null;

  return (
    <div
      className={`
        flex flex-col gap-[8px]
        p-[16px]
        bg-blue-50
        rounded-[10px]
        ${className}
      `}
    >
      {features.map((feature, index) => (
        <div key={index} className="flex items-start gap-[8px]">
          <span
            className="
              checkmark
              text-[#364153]
              text-[14px]
              font-normal
              leading-[20px]
              tracking-[-0.1504px]
              shrink-0
            "
          >
            ✓
          </span>
          <p
            className="
              text-[#364153]
              text-[14px]
              font-normal
              leading-[20px]
              tracking-[-0.1504px]
            "
          >
            {feature}
          </p>
        </div>
      ))}
    </div>
  );
};
