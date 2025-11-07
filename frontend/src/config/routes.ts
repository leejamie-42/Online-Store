export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  FORGOT_PASSWORD: "/forgot-password",

  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  CART: "/cart",
  CHECKOUT: "/checkout",
  CHECKOUT_REVIEW: "/checkout/review",
  CHECKOUT_DETAILS: "/checkout/details",
  CHECKOUT_PAYMENT: "/checkout/payment", // Legacy route (redirects)
  CHECKOUT_PAYMENT_ID: (paymentId: string) => `/checkout/payment/${paymentId}`,
  CHECKOUT_CONFIRMATION: (orderId: string) =>
    `/checkout/confirmation/${orderId}`,

  ORDERS: "/orders",
  ORDER_DETAIL: (orderId: string) => `/orders/${orderId}`,

  PROFILE: "/profile",
  SETTINGS: "/settings",

  NOT_FOUND: "*",
} as const;
