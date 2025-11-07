import React, { forwardRef } from 'react';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
  variant?: 'default' | 'filled';
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, error, helperText, fullWidth = false, variant = 'default', className = '', ...props }, ref) => {
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

        <textarea
          ref={ref}
          className={`
            w-full px-4 py-2
            rounded-lg
            focus:outline-none
            disabled:bg-gray-100 disabled:cursor-not-allowed
            placeholder:text-gray-400
            resize-none
            ${variantStyles[variant]}
            ${className}
          `}
          {...props}
        />

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

Textarea.displayName = 'Textarea';
