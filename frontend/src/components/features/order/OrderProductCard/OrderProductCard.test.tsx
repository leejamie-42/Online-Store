import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { OrderProductCard } from './OrderProductCard';
import type { OrderProduct } from './OrderProductCard';

describe('OrderProductCard', () => {
  const mockProduct: OrderProduct = {
    id: 'PRD-001',
    name: 'Wireless Mouse',
    price: 63.98,
    quantity: 2,
    imageUrl: '/images/mouse.jpg',
  };

  it('renders product information correctly', () => {
    render(<OrderProductCard product={mockProduct} />);

    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    expect(screen.getByText('Quantity: 2')).toBeInTheDocument();
    expect(screen.getByText('$63.98')).toBeInTheDocument();
  });

  it('displays product image with correct attributes', () => {
    render(<OrderProductCard product={mockProduct} />);

    const image = screen.getByAltText('Wireless Mouse') as HTMLImageElement;
    expect(image).toBeInTheDocument();
    expect(image.src).toContain('/images/mouse.jpg');
  });

  it('formats price correctly as USD currency', () => {
    render(<OrderProductCard product={mockProduct} />);

    const priceElement = screen.getByText('$63.98');
    expect(priceElement).toBeInTheDocument();
  });

  it('formats price with different values correctly', () => {
    const expensiveProduct = { ...mockProduct, price: 1234.56 };
    render(<OrderProductCard product={expensiveProduct} />);

    expect(screen.getByText('$1,234.56')).toBeInTheDocument();
  });

  it('handles single quantity', () => {
    const singleProduct = { ...mockProduct, quantity: 1 };
    render(<OrderProductCard product={singleProduct} />);

    expect(screen.getByText('Quantity: 1')).toBeInTheDocument();
  });

  it('handles large quantities', () => {
    const bulkProduct = { ...mockProduct, quantity: 100 };
    render(<OrderProductCard product={bulkProduct} />);

    expect(screen.getByText('Quantity: 100')).toBeInTheDocument();
  });

  it('applies custom className when provided', () => {
    const { container } = render(
      <OrderProductCard product={mockProduct} className="custom-class" />
    );

    const mainDiv = container.firstChild as HTMLElement;
    expect(mainDiv).toHaveClass('custom-class');
  });

  it('renders with all required flex layout classes', () => {
    const { container } = render(<OrderProductCard product={mockProduct} />);

    const mainDiv = container.firstChild as HTMLElement;
    expect(mainDiv).toHaveClass('flex', 'items-center', 'gap-4');
  });

  it('image has correct styling classes', () => {
    render(<OrderProductCard product={mockProduct} />);

    const image = screen.getByAltText('Wireless Mouse');
    expect(image).toHaveClass('w-20', 'h-20', 'object-cover', 'rounded-lg');
  });

  it('handles zero price', () => {
    const freeProduct = { ...mockProduct, price: 0 };
    render(<OrderProductCard product={freeProduct} />);

    expect(screen.getByText('$0.00')).toBeInTheDocument();
  });
});
