import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { ProductSkeleton } from './ProductSkeleton';

describe('ProductSkeleton', () => {
  it('renders skeleton with all placeholder elements', () => {
    const { container } = render(<ProductSkeleton />);

    // Should have skeleton structure
    expect(container.firstChild).toBeInTheDocument();
    expect(container.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('renders image placeholder', () => {
    const { container } = render(<ProductSkeleton />);

    // Should have a bg-gray-300 element for image placeholder
    const imagePlaceholder = container.querySelector('.bg-gray-300');
    expect(imagePlaceholder).toBeInTheDocument();
  });

  it('renders multiple text placeholders', () => {
    const { container } = render(<ProductSkeleton />);

    // Should have multiple bg-gray-200 elements for text placeholders
    const textPlaceholders = container.querySelectorAll('.bg-gray-200');
    expect(textPlaceholders.length).toBeGreaterThan(0);
  });

  it('applies animate-pulse class for animation', () => {
    const { container } = render(<ProductSkeleton />);

    expect(container.querySelector('.animate-pulse')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(<ProductSkeleton className="custom-class" />);

    expect(container.firstChild).toHaveClass('custom-class');
  });
});
