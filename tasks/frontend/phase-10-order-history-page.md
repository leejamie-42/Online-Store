# Phase 10: Order History Page Implementation

## Overview

Implement the Order History page that displays a user's order list with search, filtering, and navigation capabilities based on the provided Figma design.

**Estimated Duration:** 10-14 hours (1.5-2 days)

**Priority:** HIGH

**Prerequisites:**

- Phase 0-9 completed
- Order API service exists (`orderService.getUserOrders()`)
- Order types defined (`OrderHistoryResponse`, `OrderStatus`)
- OrderDetail page implemented (`/orders/:orderId` route)

---

## Architecture & Existing Resources

### Already Implemented

- ✅ Order types: `OrderHistoryResponse`, `OrderStatus` (in `types/order.types.ts`)
- ✅ API service: `orderService.getUserOrders()` (in `api/services/order.service.ts`)
- ✅ Mock data: `mockOrderHistory` (in `mocks/order.mock.ts`)
- ✅ Order utilities: `order.utils.ts`
- ✅ OrderDetail page: `/orders/:orderId`

### To Be Created

- OrderHistory page component
- OrderCard component (for list items)
- Search and filter functionality
- Empty state component
- Route configuration

---

## Deliverables

1. **OrderCard Component** - Individual order card in the list
2. **OrderListEmpty Component** - Empty state when no orders
3. **OrderHistory Page** - Main page with search, filter, and list
4. **Route Configuration** - `/orders` route setup
5. **Search & Filter Logic** - Client-side filtering implementation
6. **Responsive Design** - Mobile, tablet, desktop layouts
7. **Integration Tests** - Component and page tests

---

## Implementation Tasks

### Task 10.1: Create OrderCard Component

**Time:** 2-2.5 hours

Create a reusable order card component for displaying order summaries in the list.

**Figma Requirements:**

- Order ID with "Order #" prefix
- Order date with "Placed on" label
- Status badge (color-coded: green for delivered, etc.)
- Product thumbnail (first product in order)
- Product name, quantity, and price
- "View Details" button

**Files to Create:**

```
src/components/features/order/OrderCard/
├── OrderCard.tsx
├── OrderCard.test.tsx
└── index.ts
```

**Key Props:**

```typescript
interface OrderCardProps {
  order: OrderHistoryResponse;
  onViewDetails: (orderId: string) => void;
  className?: string;
}
```

---

### Task 10.2: Create OrderListEmpty Component

**Time:** 45 minutes - 1 hour

Create an empty state component for when user has no orders.

**Files to Create:**

```
src/components/features/order/OrderListEmpty/
├── OrderListEmpty.tsx
├── OrderListEmpty.test.tsx
└── index.ts
```

**Design:** Simple centered message with icon and "Start Shopping" button

---

### Task 10.3: Implement Search Functionality

**Time:** 1.5-2 hours

Add search capability to filter orders by order ID or product name.

**Requirements:**

- Debounced search input (300ms delay)
- Search by order ID (e.g., "ORD-1760604922528")
- Search by product name (e.g., "Wireless Mouse")
- Case-insensitive matching
- Clear search button (X icon)

**Implementation:** Create `useOrderSearch` custom hook

---

### Task 10.4: Implement Status Filter

**Time:** 1-1.5 hours

Add dropdown filter to show orders by status.

**Filter Options:**

- All Statuses (default)
- Pending
- Processing
- Picked Up
- Delivering
- Delivered
- Cancelled

**Implementation:** Use existing Select/Dropdown component with filter logic

---

### Task 10.5: Create OrderHistory Page

**Time:** 3-4 hours

Main page component integrating all features.

**Files to Create:**

```
src/pages/OrderHistory.tsx
src/pages/OrderHistory.test.tsx
```

**Layout Structure:**

```
OrderHistory Page
├── Header
│   ├── Title: "Order History"
│   └── Subtitle: "View and track all your orders"
├── Filters Section
│   ├── Search Input (left)
│   └── Status Dropdown (right)
└── Order List
    ├── OrderCard (repeated)
    ├── OrderListEmpty (if no orders)
    └── Loading State
```

**State Management:**

- Fetch orders using `orderService.getUserOrders()`
- Apply search and filter client-side
- Handle loading, error, and empty states
- Navigate to order detail on "View Details" click

---

### Task 10.6: Configure Routes

**Time:** 20-30 minutes

Add OrderHistory page to router configuration.

**Routes:**

- `/orders` → OrderHistory page (list)
- `/orders/:orderId` → OrderDetail page (existing)

**Authentication:** Both routes require login (ProtectedRoute wrapper)

---

### Task 10.7: Style & Design Alignment

**Time:** 2-3 hours

Align implementation with Figma design specifications.

**Color Palette (from Figma):**

- Delivered: Green badge (`bg-green-100 text-green-800`)
- Pending/Processing: Yellow/Orange
- Cancelled: Red badge
- Card background: White (`bg-white`)
- Border: Light gray (`border-gray-200`)

**Typography:**

- Page title: 28px/32px, font-semibold
- Subtitle: 16px, text-gray-600
- Order ID: 16px, font-medium
- Date: 14px, text-gray-600
- Status badge: 13px, font-medium

**Spacing:**

- Page padding: 24px (p-6)
- Card gap: 16px (gap-4)
- Card padding: 20px (p-5)

---

### Task 10.8: Responsive Design

**Time:** 1.5-2 hours

Ensure page works on all device sizes.

**Breakpoints:**

- **Mobile (<640px):** Single column, stacked filters
- **Tablet (640-1024px):** Single column with comfortable spacing
- **Desktop (>1024px):** Optimized width (max-w-7xl), filters side-by-side

**Mobile Optimizations:**

- Stack search and filter vertically
- Full-width order cards
- Touch-friendly buttons (min 44px height)
- Simplified order card layout

---

### Task 10.9: Testing & Validation

**Time:** 2-3 hours

Write comprehensive tests for all components and page functionality.

**Test Coverage:**

- OrderCard component rendering
- Search functionality (debounce, filtering)
- Status filter logic
- Empty state display
- Navigation to order detail
- Loading and error states
- Responsive layout

**Testing Tools:** React Testing Library, Jest, MSW (Mock Service Worker)

---

## Technical Implementation Details

### Search Hook Implementation

```typescript
// hooks/useOrderSearch.ts
export const useOrderSearch = (
  orders: OrderHistoryResponse[],
  searchTerm: string,
  statusFilter: OrderStatus | "all"
) => {
  const filteredOrders = useMemo(() => {
    let results = orders;

    // Status filter
    if (statusFilter !== "all") {
      results = results.filter((order) => order.status === statusFilter);
    }

    // Search filter
    if (searchTerm) {
      const lowerSearch = searchTerm.toLowerCase();
      results = results.filter(
        (order) =>
          order.orderId.toString().includes(lowerSearch) ||
          order.products.some((p) => p.name.toLowerCase().includes(lowerSearch))
      );
    }

    return results;
  }, [orders, searchTerm, statusFilter]);

  return filteredOrders;
};
```

### Status Badge Component

```typescript
// components/ui/StatusBadge/StatusBadge.tsx
const STATUS_STYLES: Record<OrderStatus, string> = {
  delivered: "bg-green-100 text-green-800",
  delivering: "bg-blue-100 text-blue-800",
  processing: "bg-yellow-100 text-yellow-800",
  pending: "bg-gray-100 text-gray-800",
  picked_up: "bg-blue-100 text-blue-800",
  cancelled: "bg-red-100 text-red-800",
};
```

---

## Integration with Existing Code

### Using Existing Services

```typescript
// In OrderHistory.tsx
import { orderService } from "@/api/services/order.service";
import { useQuery } from "@tanstack/react-query";

const {
  data: orders,
  isLoading,
  error,
} = useQuery({
  queryKey: ["orders"],
  queryFn: orderService.getUserOrders,
});
```

### Navigation to Order Detail

```typescript
// In OrderCard.tsx
import { useNavigate } from "react-router-dom";

const navigate = useNavigate();
const handleViewDetails = () => {
  navigate(`/orders/${order.orderId}`);
};
```

---

## Acceptance Criteria

### Functional Requirements

- [ ] Page displays all user orders from API
- [ ] Search filters orders by ID or product name
- [ ] Status dropdown filters orders correctly
- [ ] "View Details" navigates to OrderDetail page
- [ ] Empty state shows when no orders match filters
- [ ] Loading state shows during data fetch
- [ ] Error state handles API failures gracefully

### Design Requirements

- [ ] Layout matches Figma design
- [ ] Colors and typography are pixel-perfect
- [ ] Status badges use correct color coding
- [ ] Spacing matches design specifications
- [ ] Order cards display all required information

### Technical Requirements

- [ ] TypeScript has no errors
- [ ] All components are properly typed
- [ ] Tests achieve >80% code coverage
- [ ] No console errors or warnings
- [ ] Responsive design works on all devices

### Performance Requirements

- [ ] Search is debounced for performance
- [ ] Order list renders efficiently (no lag)
- [ ] Images load with proper optimization
- [ ] Page loads in <2 seconds

---

## Common Issues & Solutions

### Issue: Search is too slow with many orders

**Solution:** Use debounced input (300ms) and useMemo for filtered results

### Issue: Status filter doesn't reset when searching

**Solution:** Maintain independent filter states and combine filters in hook

### Issue: Images not loading in order cards

**Solution:** Use fallback image placeholder and handle loading states

---

## Next Steps

After Phase 10 completion:

1. **Phase 11:** Order tracking with real-time updates
2. **Phase 12:** Order cancellation feature
3. **Phase 13:** Export order history to PDF/CSV

---

## Resources

**Documentation:**

- React Query: https://tanstack.com/query/latest
- React Router: https://reactrouter.com/
- Tailwind CSS: https://tailwindcss.com/

**Internal Docs:**

- `docs/frontend/ARCHITECTURE.md` - Component patterns
- `docs/frontend/UI_DESIGN_SYSTEM.md` - Design tokens
- `docs/frontend/API_INTEGRATION.md` - API specs

**Related Files:**

- `src/types/order.types.ts` - Order type definitions
- `src/api/services/order.service.ts` - Order API service
- `src/pages/OrderDetail.tsx` - Related order detail page
