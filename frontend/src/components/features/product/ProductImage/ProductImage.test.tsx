import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductImage } from './ProductImage';

describe('ProductImage', () => {
  const defaultProps = {
    src: 'https://example.com/product.jpg',
    alt: 'Test Product',
  };

  it('renders image with correct src and alt', () => {
    render(<ProductImage {...defaultProps} />);

    const img = screen.getByAltText('Test Product');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', 'https://example.com/product.jpg');
  });

  it('displays fallback image on error', () => {
    render(<ProductImage {...defaultProps} />);

    const img = screen.getByAltText('Test Product') as HTMLImageElement;

    // Simulate image load error
    fireEvent.error(img);

    // Should switch to fallback src
    expect(img.src).toContain('/placeholder-product.png');
  });

  it('shows loading state while image loads', () => {
    render(<ProductImage {...defaultProps} />);

    // Initially should show loading state
    const container = screen.getByAltText('Test Product').parentElement;
    expect(container).toHaveClass('bg-gray-200');
  });

  it('applies custom className', () => {
    const { container } = render(
      <ProductImage {...defaultProps} className="custom-class" />
    );

    expect(container.firstChild).toHaveClass('custom-class');
  });

  it('applies custom imageClassName to img element', () => {
    render(<ProductImage {...defaultProps} imageClassName="rounded-lg" />);

    const img = screen.getByAltText('Test Product');
    expect(img).toHaveClass('rounded-lg');
  });

  it('supports different sizes via className', () => {
    const { container } = render(
      <ProductImage {...defaultProps} className="w-64 h-64" />
    );

    expect(container.firstChild).toHaveClass('w-64', 'h-64');
  });
});
