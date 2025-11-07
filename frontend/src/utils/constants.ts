export const API_TIMEOUT = 15000; // 15 seconds

export const STORAGE_KEYS = {
  AUTH_TOKEN: "authToken",
  REFRESH_TOKEN: "refreshToken",
  USER: "user",
  CART: "cart",
} as const;

export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  PRODUCTS: "/products",
  PRODUCT_DETAIL: "/products/:id",
  CART: "/cart",
  CHECKOUT: "/checkout",
  ORDERS: "/orders",
  ORDER_DETAIL: "/orders/:orderId",
  PROFILE: "/profile",
} as const;

export const ID_PREFIX = {
  PRODUCT: "PRO",
  ORDER: "ORD",
  USER: "U",
} as const;
