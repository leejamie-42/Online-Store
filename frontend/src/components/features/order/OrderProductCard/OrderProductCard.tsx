import React from 'react';

export interface OrderProduct {
  id: string;
  name: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

export interface OrderProductCardProps {
  product: OrderProduct;
  className?: string;
}

export const OrderProductCard: React.FC<OrderProductCardProps> = ({
  product,
  className = '',
}) => {
  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  return (
    <div className={`flex items-center gap-4 ${className}`}>
      {/* Product Image */}
      <div className="flex-shrink-0">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-20 h-20 object-cover rounded-lg"
        />
      </div>

      {/* Product Details */}
      <div className="flex-1">
        <h3 className="text-base font-medium text-gray-900">
          {product.name}
        </h3>
        <p className="text-sm text-gray-600 mt-1">
          Quantity: {product.quantity}
        </p>
        <p className="text-base font-semibold text-gray-900 mt-1">
          {formatPrice(product.price)}
        </p>
      </div>
    </div>
  );
};
