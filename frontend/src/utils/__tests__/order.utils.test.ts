/**
 * Order Utilities Tests
 * Unit tests for order calculation and formatting functions
 */

import { describe, it, expect } from 'vitest';
import {
  calculateCartSummary,
  formatCurrency,
  getShippingDescription,
  calculateTax,
  qualifiesForFreeShipping,
} from '../order.utils';
import type { CartItem } from '@/types/cart.types';

describe('calculateCartSummary', () => {
  it('calculates subtotal correctly for single item', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 50, stock: 10, image_url: '', published: true },
        quantity: 2,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.subtotal).toBe(100);
  });

  it('calculates subtotal correctly for multiple items', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 50, stock: 10, image_url: '', published: true },
        quantity: 2,
      },
      {
        product: { id: '2', name: 'Product 2', price: 30, stock: 5, image_url: '', published: true },
        quantity: 1,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.subtotal).toBe(130);
  });

  it('applies free shipping for orders >= $100', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 100, stock: 10, image_url: '', published: true },
        quantity: 1,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.shipping).toBe(0);
  });

  it('applies $10 shipping for orders < $100', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 50, stock: 10, image_url: '', published: true },
        quantity: 1,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.shipping).toBe(10);
  });

  it('calculates 8% tax on subtotal', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 100, stock: 10, image_url: '', published: true },
        quantity: 1,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.tax).toBe(8); // 8% of 100
  });

  it('calculates total = subtotal + shipping + tax', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 50, stock: 10, image_url: '', published: true },
        quantity: 1,
      },
    ];
    const result = calculateCartSummary(items);
    // subtotal: 50, shipping: 10, tax: 4 (8% of 50) = 64
    expect(result.total).toBe(64);
  });

  it('counts total items correctly', () => {
    const items: CartItem[] = [
      {
        product: { id: '1', name: 'Product 1', price: 50, stock: 10, image_url: '', published: true },
        quantity: 2,
      },
      {
        product: { id: '2', name: 'Product 2', price: 30, stock: 5, image_url: '', published: true },
        quantity: 3,
      },
    ];
    const result = calculateCartSummary(items);
    expect(result.itemCount).toBe(5);
  });

  it('handles empty cart', () => {
    const result = calculateCartSummary([]);
    expect(result.subtotal).toBe(0);
    expect(result.shipping).toBe(10); // Still charges shipping for empty cart
    expect(result.tax).toBe(0);
    expect(result.total).toBe(10);
    expect(result.itemCount).toBe(0);
  });
});

describe('formatCurrency', () => {
  it('formats currency with AUD symbol', () => {
    expect(formatCurrency(99.99)).toBe('$99.99');
  });

  it('formats whole numbers with .00', () => {
    expect(formatCurrency(100)).toBe('$100.00');
  });

  it('rounds to 2 decimal places', () => {
    expect(formatCurrency(99.999)).toBe('$100.00');
  });

  it('handles zero correctly', () => {
    expect(formatCurrency(0)).toBe('$0.00');
  });
});

describe('getShippingDescription', () => {
  it('returns "FREE" for subtotal >= $100', () => {
    expect(getShippingDescription(100)).toBe('FREE');
    expect(getShippingDescription(150)).toBe('FREE');
  });

  it('returns formatted shipping cost for subtotal < $100', () => {
    expect(getShippingDescription(50)).toBe('$10.00');
    expect(getShippingDescription(99.99)).toBe('$10.00');
  });
});

describe('calculateTax', () => {
  it('calculates 8% tax correctly', () => {
    expect(calculateTax(100)).toBe(8);
    expect(calculateTax(50)).toBe(4);
    expect(calculateTax(25)).toBe(2);
  });

  it('handles zero subtotal', () => {
    expect(calculateTax(0)).toBe(0);
  });
});

describe('qualifiesForFreeShipping', () => {
  it('returns true for subtotal >= $100', () => {
    expect(qualifiesForFreeShipping(100)).toBe(true);
    expect(qualifiesForFreeShipping(150)).toBe(true);
  });

  it('returns false for subtotal < $100', () => {
    expect(qualifiesForFreeShipping(99.99)).toBe(false);
    expect(qualifiesForFreeShipping(50)).toBe(false);
  });
});
