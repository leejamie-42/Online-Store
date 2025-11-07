# API Type Alignment Fixes

**Priority**: 🟡 Important  
**Estimated Effort**: 2-4 hours  
**Complexity**: Medium

## Overview

Fix type mismatches between frontend TypeScript types and backend Java DTOs to ensure type safety and prevent runtime errors.

---

## Issues Found

### 1. Order Types Mismatch
**Problem**: Frontend uses generic `Order` type, backend returns specific DTOs

**Backend DTOs**:
- `OrderDetailResponse` (GET /api/orders/{id})
- `OrderSummaryResponse` (GET /api/orders)

### 2. BPAY Info Type Name Differs
**Problem**: Frontend `BpayDetails` vs Backend `BpayInfoResponse`

### 3. Refund Response Inline Type
**Problem**: Frontend uses inline object, backend returns `RefundResponse` DTO

### 4. Field Naming Convention Inconsistent
**Problem**: Mixed camelCase and snake_case usage

---

## Task 1: Align Order Response Types

### Backend DTOs Reference

**OrderDetailResponse.java**:
```java
{
  "orderId": Long,
  "userId": Long,
  "productId": Long,
  "productName": String,
  "productPrice": BigDecimal,
  "quantity": Integer,
  "totalAmount": BigDecimal,
  "status": String,
  "shippingInfo": ShippingInfoDto,
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime
}
```

**OrderSummaryResponse.java**:
```java
{
  "orderId": Long,
  "productId": Long,
  "productName": String,
  "quantity": Integer,
  "totalAmount": BigDecimal,
  "status": String,
  "customerName": String,
  "createdAt": LocalDateTime
}
```

### Frontend Changes

#### Step 1.1: Update `types/order.types.ts`

```typescript
/**
 * Order Detail Response
 * Matches OrderDetailResponse.java
 * GET /api/orders/{id}
 */
export interface OrderDetailResponse {
  orderId: number;
  userId: number;
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  totalAmount: number;
  status: OrderStatus;
  shippingInfo: ShippingInfo;
  createdAt: string;
  updatedAt: string;
}

/**
 * Order Summary Response
 * Matches OrderSummaryResponse.java
 * GET /api/orders
 */
export interface OrderSummaryResponse {
  orderId: number;
  productId: number;
  productName: string;
  quantity: number;
  totalAmount: number;
  status: OrderStatus;
  customerName: string;
  createdAt: string;
}

/**
 * Shipping Information (camelCase)
 */
export interface ShippingInfo {
  firstName: string;
  lastName: string;
  email: string;
  mobileNumber: string;
  addressLine1: string;
  city: string;
  state: string;
  postcode: string;
  country: string;
}
```

#### Step 1.2: Update `api/services/order.service.ts`

```typescript
// Before
getOrder: async (orderId: string): Promise<Order> => {
  const response = await apiClient.get<Order>(`/orders/${orderId}`);
  return response.data;
}

// After
getOrder: async (orderId: string): Promise<OrderDetailResponse> => {
  const response = await apiClient.get<OrderDetailResponse>(`/orders/${orderId}`);
  return response.data;
},

getUserOrders: async (): Promise<OrderSummaryResponse[]> => {
  const response = await apiClient.get<OrderSummaryResponse[]>("/orders");
  return response.data;
}
```

#### Step 1.3: Update Components

Search for usages:
```bash
grep -r "Order\[\]" src/
grep -r ": Order" src/
```

Update component props and field access:
```typescript
// Before
{orders?.map((order) => (
  <OrderCard key={order.id} orderId={order.id} />
))}

// After
{orders?.map((order) => (
  <OrderCard key={order.orderId} orderId={order.orderId} />
))}
```

---

## Task 2: Align BPAY Info Response Type

### Backend DTO Reference

**BpayInfoResponse.java**:
```java
{
  "billerCode": String,
  "referenceNumber": String,
  "amount": BigDecimal,
  "expiresAt": LocalDateTime
}
```

### Frontend Changes

#### Step 2.1: Update `types/order.types.ts`

```typescript
/**
 * BPAY Info Response
 * Matches BpayInfoResponse.java
 * GET /api/payments/{id}
 */
export interface BpayInfoResponse {
  billerCode: string;
  referenceNumber: string;
  amount: number;
  expiresAt: string;
}

// Backward compatibility alias (optional)
/** @deprecated Use BpayInfoResponse */
export type BpayDetails = BpayInfoResponse;
```

#### Step 2.2: Update `api/services/payment.service.ts`

```typescript
// Before
getPaymentDetails: async (paymentId: string): Promise<BpayDetails> => {
  const response = await apiClient.get<BpayDetails>(`/payments/${paymentId}`);
  return response.data;
}

// After
getPaymentDetails: async (paymentId: string): Promise<BpayInfoResponse> => {
  const response = await apiClient.get<BpayInfoResponse>(`/payments/${paymentId}`);
  return response.data;
}
```

#### Step 2.3: Update Components

Search for usages:
```bash
grep -r "BpayDetails" src/
grep -r "biller_code" src/
```

Update property access:
```typescript
// Before
<p>Biller Code: {bpayDetails.biller_code}</p>
<p>Reference: {bpayDetails.reference_number}</p>

// After
<p>Biller Code: {bpayInfo.billerCode}</p>
<p>Reference: {bpayInfo.referenceNumber}</p>
```

---

## Task 3: Align Refund Response Type

### Backend DTO Reference

**RefundResponse.java**:
```java
{
  "paymentId": Long,
  "status": String,
  "refundedAt": LocalDateTime
}
```

### Frontend Changes

#### Step 3.1: Create `types/payment.types.ts` (optional)

```typescript
/**
 * Refund Response
 * Matches RefundResponse.java
 * POST /api/payments/{id}/refund
 */
export interface RefundResponse {
  paymentId: number;
  status: string;
  refundedAt: string | null;
}
```

#### Step 3.2: Update `api/services/payment.service.ts`

```typescript
// Before
refundPayment: async (
  paymentId: string,
  reason: string,
): Promise<{ payment_id: string; status: string; refunded_at: string }> => {
  const response = await apiClient.post(`/payments/${paymentId}/refund`, { reason });
  return response.data;
}

// After
refundPayment: async (
  paymentId: string,
  reason: string,
): Promise<RefundResponse> => {
  const response = await apiClient.post<RefundResponse>(
    `/payments/${paymentId}/refund`,
    { reason }
  );
  return response.data;
}
```

---

## Task 4: Remove Non-Existent Endpoint

### Issue
Frontend calls `POST /api/orders/:id/confirm-payment` but endpoint doesn't exist in backend.

### Solution
Comment out method in `order.service.ts`:

```typescript
/**
 * ⚠️ DEPRECATED: Endpoint does not exist in backend
 * Payment confirmation handled via Bank webhook
 */
// confirmPayment: async (
//   orderId: string,
//   data: ConfirmPaymentRequest,
// ): Promise<void> => {
//   await apiClient.post(`/orders/${orderId}/confirm-payment`, data);
// },
```

Update UI to poll for order status instead:
```typescript
const { data: order } = useQuery({
  queryKey: ['order', orderId],
  queryFn: () => orderService.getOrder(orderId),
  refetchInterval: 5000, // Poll for status updates
});
```

---

## Implementation Plan

### Phase 1: Type Definitions (1 hour)
- [ ] Update `OrderDetailResponse` and `OrderSummaryResponse`
- [ ] Rename `BpayDetails` to `BpayInfoResponse`
- [ ] Create `RefundResponse` type
- [ ] Update `ShippingInfo` to camelCase

### Phase 2: Service Layer (30 min)
- [ ] Update order service return types
- [ ] Update payment service return types
- [ ] Remove `confirmPayment` method

### Phase 3: Components (1-2 hours)
- [ ] Update components using Order types
- [ ] Update components using BPAY types
- [ ] Fix property access to use camelCase

### Phase 4: Testing (30 min)
- [ ] Run `npm run type-check`
- [ ] Test order list page
- [ ] Test order detail page
- [ ] Test BPAY payment page

---

## Testing Checklist

### TypeScript Compilation
```bash
cd frontend
npm run type-check
```
Expected: ✅ No type errors

### Functional Testing
- [ ] Order creation returns correct format
- [ ] Order list displays correctly
- [ ] Order detail displays correctly
- [ ] BPAY instructions display correctly
- [ ] No console errors

---

## Files to Modify

```
frontend/src/
├── types/
│   ├── order.types.ts        ← Main updates
│   └── payment.types.ts      ← Create new (optional)
├── api/services/
│   ├── order.service.ts      ← Update return types
│   └── payment.service.ts    ← Update return types
└── [Components using these types]
```

---

## Success Criteria

✅ TypeScript compilation passes  
✅ All types match backend DTOs  
✅ Consistent camelCase naming  
✅ No runtime type errors  
✅ All pages render correctly
