import React from 'react';
import { LuMinus, LuPlus } from 'react-icons/lu';

interface QuantitySelectorProps {
  value: number;
  onChange: (quantity: number) => void;
  min?: number;
  max?: number;
  className?: string;
}

/**
 * QuantitySelector Component
 * Input for selecting product quantity
 * Updated to match Figma design specs
 */
export const QuantitySelector: React.FC<QuantitySelectorProps> = ({
  value,
  onChange,
  min = 1,
  max = 99,
  className = '',
}) => {
  const handleDecrement = () => {
    if (value > min) {
      onChange(value - 1);
    }
  };

  const handleIncrement = () => {
    if (value < max) {
      onChange(value + 1);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = parseInt(e.target.value);
    if (isNaN(newValue)) return;

    // Clamp value between min and max
    const clampedValue = Math.min(Math.max(newValue, min), max);
    onChange(clampedValue);
  };

  return (
    <div className={`flex items-center gap-[12px] ${className}`}>
      <button
        type="button"
        onClick={handleDecrement}
        disabled={value <= min}
        className="
          p-2
          bg-white
          border border-[rgba(0,0,0,0.1)] border-solid
          rounded-[8px]
          hover:bg-gray-50
          disabled:opacity-50
          disabled:cursor-not-allowed
          w-[36px] h-[36px]
        "
        aria-label="Decrease quantity"
      >
        <LuMinus className="w-4 h-4" />
      </button>

      <input
        type="number"
        value={value}
        onChange={handleInputChange}
        min={min}
        max={max}
        className="
          w-[80px]
          h-[36px]
          px-[12px]
          py-[4px]
          text-center
          text-[14px]
          font-normal
          leading-[20px]
          tracking-[-0.1504px]
          bg-[#f3f3f5]
          border-0
          rounded-[8px]
          focus:ring-2
          focus:ring-primary-500
        "
        aria-label="Quantity"
      />

      <button
        type="button"
        onClick={handleIncrement}
        disabled={value >= max}
        className="
          p-2
          bg-white
          border border-[rgba(0,0,0,0.1)] border-solid
          rounded-[8px]
          hover:bg-gray-50
          disabled:opacity-50
          disabled:cursor-not-allowed
          w-[36px] h-[36px]
        "
        aria-label="Increase quantity"
      >
        <LuPlus className="w-4 h-4" />
      </button>
    </div>
  );
};
