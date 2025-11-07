import React, { forwardRef } from 'react';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
  variant?: 'default' | 'filled';
  options: SelectOption[];
  placeholder?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, helperText, fullWidth = false, variant = 'default', options, placeholder, className = '', ...props }, ref) => {
    const hasError = Boolean(error);

    const variantStyles = {
      default: `
        border ${hasError ? 'border-red-500' : 'border-gray-300'}
        bg-white
        focus:ring-2 ${hasError ? 'focus:ring-red-500' : 'focus:ring-primary-500'}
        focus:border-transparent
      `,
      filled: `
        border-none
        bg-gray-100
        focus:ring-0 focus:outline-none
        ${hasError ? 'bg-red-50' : ''}
      `,
    };

    return (
      <div className={`${fullWidth ? 'w-full' : ''}`}>
        {label && (
          <label className="block text-sm font-medium text-gray-900 mb-2">
            {label}
            {props.required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}

        <select
          ref={ref}
          className={`
            w-full px-4 py-2
            rounded-lg
            focus:outline-none
            disabled:bg-gray-100 disabled:cursor-not-allowed
            appearance-none
            bg-no-repeat
            bg-[length:1.5em_1.5em]
            bg-[position:right_0.5rem_center]
            bg-[url('data:svg+xml;charset=UTF-8,%3csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22currentColor%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22%3e%3cpolyline points=%226 9 12 15 18 9%22%3e%3c/polyline%3e%3c/svg%3e')]
            ${variantStyles[variant]}
            ${className}
          `}
          {...props}
        >
          {placeholder && (
            <option value="" disabled>
              {placeholder}
            </option>
          )}
          {options.map((option) => (
            <option
              key={option.value}
              value={option.value}
              disabled={option.disabled}
            >
              {option.label}
            </option>
          ))}
        </select>

        {error && (
          <p className="mt-1 text-sm text-red-600">{error}</p>
        )}

        {helperText && !error && (
          <p className="mt-1 text-sm text-gray-500">{helperText}</p>
        )}
      </div>
    );
  }
);

Select.displayName = 'Select';
