/**
 * useCheckout Hook
 * Manages checkout flow: create order → create payment → navigate to payment page
 */

import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { useCartStore } from "@/stores/cart.store";
import { useAuth } from "@/hooks/useAuth";
import { orderService } from "@/api/services/order.service";
import { paymentService } from "@/api/services/payment.service";
import { ROUTES } from "@/config/routes";
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  CreatePaymentRequest,
  CreatePaymentResponse,
} from "@/types";
import { removePrfixToGetId } from "@/utils/formatters";

export interface UseCheckoutReturn {
  // Order creation
  orderData: CreateOrderResponse | undefined;
  isCreatingOrder: boolean;
  createOrderError: Error | null;

  // Payment creation
  paymentData: CreatePaymentResponse | undefined;
  isCreatingPayment: boolean;
  createPaymentError: Error | null;

  // Combined action
  proceedToPayment: () => void;
  isProcessing: boolean;
}

/**
 * useCheckout Hook
 *
 * Flow:
 * 1. User clicks "Proceed to Payment"
 * 2. Create order (POST /api/orders)
 * 3. Create payment (POST /api/payments)
 * 4. Navigate to payment page with payment_id
 */
export function useCheckout(): UseCheckoutReturn {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { items, shippingInfo } = useCartStore();

  /**
   * Step 1: Create Order
   */
  const createOrderMutation = useMutation<
    CreateOrderResponse,
    Error,
    CreateOrderRequest
  >({
    mutationFn: (orderData) => orderService.createOrder(orderData),
    onSuccess: (orderResponse) => {
      // After order created, create payment
      createPaymentMutation.mutate({
        orderId: orderResponse.orderId,
        method: "BPAY",
      });
    },
    onError: (error) => {
      console.error("Order creation error:", error);
    },
  });

  /**
   * Step 2: Create Payment
   */
  const createPaymentMutation = useMutation<
    CreatePaymentResponse,
    Error,
    CreatePaymentRequest
  >({
    mutationFn: (paymentData) => paymentService.createPayment(paymentData),
    onSuccess: (paymentResponse, variables) => {
      // Navigate to payment page with paymentId and orderId as query param
      navigate(
        `${ROUTES.CHECKOUT_PAYMENT_ID(String(paymentResponse.paymentId))}?orderId=${variables.orderId}`,
      );
    },
    onError: (error) => {
      console.error("Payment creation error:", error);
    },
  });

  /**
   * Combined action: Create order and payment
   */
  const proceedToPayment = () => {
    // Validation
    if (!user) {
      throw new Error("User not authenticated");
    }

    if (items.length === 0) {
      throw new Error("Cart is empty");
    }

    if (!shippingInfo) {
      throw new Error("Shipping information missing");
    }

    // Currently only support single item in cart
    const cartItem = items[0];

    console.log(removePrfixToGetId(cartItem.product.id));
    // Create order request matching API spec
    const orderData: CreateOrderRequest = {
      productId: removePrfixToGetId(cartItem.product.id),
      quantity: cartItem.quantity,
      userId: user.id,
      shippingInfo: {
        firstName: shippingInfo.firstName || "",
        lastName: shippingInfo.lastName || "",
        email: shippingInfo.email,
        mobileNumber: shippingInfo.mobileNumber || "",
        addressLine1: shippingInfo.addressLine1 || "",
        state: shippingInfo.state,
        city: shippingInfo.city,
        postcode: shippingInfo.postcode,
        country: shippingInfo.country,
      },
    };

    // Trigger order creation (will automatically trigger payment creation on success)
    createOrderMutation.mutate(orderData);
  };

  return {
    // Order state
    orderData: createOrderMutation.data,
    isCreatingOrder: createOrderMutation.isPending,
    createOrderError: createOrderMutation.error,

    // Payment state
    paymentData: createPaymentMutation.data,
    isCreatingPayment: createPaymentMutation.isPending,
    createPaymentError: createPaymentMutation.error,

    // Combined action
    proceedToPayment,
    isProcessing:
      createOrderMutation.isPending || createPaymentMutation.isPending,
  };
}
