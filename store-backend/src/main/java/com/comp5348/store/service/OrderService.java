package com.comp5348.store.service;

import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.dto.event.InventoryRollbackEvent;
import com.comp5348.store.dto.event.PaymentRefundEvent;
import com.comp5348.store.dto.order.CreateOrderRequest;
import com.comp5348.store.dto.order.CreateOrderResponse;
import com.comp5348.store.dto.order.OrderDetailResponse;
import com.comp5348.store.dto.order.OrderHistoryResponse;
import com.comp5348.store.exception.OrderNotFoundException;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.grpc.warehouse.CheckStockResponse;
import com.comp5348.store.grpc.warehouse.ReserveStockResponse;
import com.comp5348.store.model.Product;
import com.comp5348.store.model.auth.User;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.service.event.EventPublisher;
import com.comp5348.store.service.warehouse.WarehouseGrpcClient;
import com.comp5348.store.util.OrderMapper;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for order management with Warehouse gRPC integration.
 *
 * Order Creation Workflow:
 * 1. Validate user authorization (user_id matches authenticated user)</li>
 * 2. Fetch product from database</li>
 * 3. Check stock availability via Warehouse gRPC</li>
 * 4. Reserve stock via Warehouse gRPC (atomic operation)</li>
 * 5. Create Order entity with PENDING status</li>
 * 6. Save to database (transactional)</li>
 * 7. On failure: Rollback stock reservation</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 50L;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WarehouseGrpcClient warehouseClient;
    private final EventPublisher eventPublisher;

    /**
     * Create a new order with stock validation and reservation.
     *
     * @param request             the order creation request
     * @param authenticatedUserId the authenticated user ID from security context
     * @return CreateOrderResponse with order ID, status, and total
     * @throws AccessDeniedException    if user_id doesn't match authenticated user
     * @throws ProductNotFoundException if product not found
     * @throws IllegalStateException    if insufficient stock or reservation fails
     */
    @Transactional
    public CreateOrderResponse createOrder(
        CreateOrderRequest request,
        Long authenticatedUserId
    ) {
        log.info(
            "Creating order for user {} product {} quantity {}",
            request.getUserId(),
            request.getProductId(),
            request.getQuantity()
        );

        // 1. Validate user authorization
        if (!request.getUserId().equals(authenticatedUserId)) {
            log.warn(
                "Unauthorized order creation attempt: user {} tried to create order for user {}",
                authenticatedUserId,
                request.getUserId()
            );
            throw new AccessDeniedException(
                "Cannot create order for another user. User ID mismatch."
            );
        }

        // 2. Fetch user and product
        User user = userRepository
            .findById(request.getUserId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "User not found: " + request.getUserId()
                )
            );

        Product product = productRepository
            .findById(request.getProductId())
            .orElseThrow(() ->
                new ProductNotFoundException(request.getProductId())
            );

        // 3. Check stock availability via Warehouse
        log.debug(
            "Checking stock availability for product {} quantity {}",
            request.getProductId(),
            request.getQuantity()
        );

        CheckStockResponse stockCheck;
        try {
            stockCheck = warehouseClient.checkStock(
                request.getProductId(),
                request.getQuantity()
            );
        } catch (StatusRuntimeException e) {
            log.error("Warehouse gRPC check stock failed: {}", e.getStatus());
            throw new IllegalStateException(
                "Unable to check stock availability: " +
                    e.getStatus().getDescription()
            );
        }

        if (!stockCheck.getAvailable()) {
            log.warn(
                "Insufficient stock for product {} quantity {}. Available: {}",
                request.getProductId(),
                request.getQuantity(),
                stockCheck.getTotalAvailable()
            );
            throw new IllegalStateException(
                String.format(
                    "Insufficient stock. Requested: %d, Available: %d",
                    request.getQuantity(),
                    stockCheck.getTotalAvailable()
                )
            );
        }

        // 4. Create order entity (not saved yet, need order ID for reservation)
        BigDecimal totalAmount = product
            .getPrice()
            .multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
            .user(user)
            .product(product)
            .quantity(request.getQuantity())
            .status(OrderStatus.PENDING)
            .totalAmount(totalAmount)
            // Shipping info
            .firstName(request.getShippingInfo().getFirstName())
            .lastName(request.getShippingInfo().getLastName())
            .email(request.getShippingInfo().getEmail())
            .mobileNumber(request.getShippingInfo().getMobileNumber())
            .addressLine1(request.getShippingInfo().getAddressLine1())
            .city(request.getShippingInfo().getCity())
            .state(request.getShippingInfo().getState())
            .postcode(request.getShippingInfo().getPostcode())
            .country(request.getShippingInfo().getCountry())
            .build();

        // 5. Save order to get ID (for warehouse reservation tracking)
        order = orderRepository.save(order);
        log.info("Order created with ID: {}", order.getId());

        // 6. Reserve stock via Warehouse gRPC with retry logic
        try {
            log.debug(
                "Reserving stock for order {} product {} quantity {}",
                order.getId(),
                request.getProductId(),
                request.getQuantity()
            );

            ReserveStockResponse reservation = reserveStockWithRetry(
                request.getProductId(),
                request.getQuantity(),
                order.getId()
            );

            if (!reservation.getSuccess()) {
                log.error(
                    "Stock reservation failed for order {}: {}",
                    order.getId(),
                    reservation.getMessage()
                );
                // Rollback: Delete the order since reservation failed
                orderRepository.delete(order);
                throw new IllegalStateException(
                    "Stock reservation failed: " + reservation.getMessage()
                );
            }

            log.info(
                "Stock reserved successfully for order {} from warehouses: {}",
                order.getId(),
                reservation.getReservedFromWarehousesList()
            );
        } catch (StatusRuntimeException e) {
            log.error(
                "Warehouse gRPC reserve stock failed for order {}: {}",
                order.getId(),
                e.getStatus()
            );
            // Rollback: Delete the order since gRPC failed
            orderRepository.delete(order);
            throw new IllegalStateException(
                "Unable to reserve stock: " + e.getStatus().getDescription()
            );
        }

        // 7. Return success response
        return OrderMapper.toCreateOrderResponse(order);
    }

    /**
     * Get order details by ID with user authorization.
     *
     * @param orderId             the order ID
     * @param authenticatedUserId the authenticated user ID
     * @return OrderDetailResponse with complete order information
     * @throws IllegalArgumentException if order not found
     * @throws AccessDeniedException    if user doesn't own the order
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(
        Long orderId,
        Long authenticatedUserId
    ) {
        log.debug(
            "Fetching order {} for user {}",
            orderId,
            authenticatedUserId
        );

        Order order = orderRepository
            .findByIdAndUserId(orderId, authenticatedUserId)
            .orElseThrow(() -> {
                // Check if order exists at all
                if (orderRepository.existsById(orderId)) {
                    log.warn(
                        "User {} attempted to access order {} owned by another user",
                        authenticatedUserId,
                        orderId
                    );
                    throw new AccessDeniedException(
                        "You do not have permission to access this order"
                    );
                } else {
                    log.warn("Order {} not found", orderId);
                    throw new IllegalArgumentException(
                        "Order not found: " + orderId
                    );
                }
            });

        return OrderMapper.toOrderDetailResponse(order);
    }

    /**
     * Get all orders for a user.
     *
     * @param authenticatedUserId the authenticated user ID
     * @return list of OrderSummaryResponse ordered by creation date descending
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getUserOrders(Long authenticatedUserId) {
        log.debug("Fetching orders for user {}", authenticatedUserId);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
            authenticatedUserId
        );

        log.debug(
            "Found {} orders for user {}",
            orders.size(),
            authenticatedUserId
        );

        return orders
            .stream()
            .map(OrderMapper::toOrderHistoryResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderDetailResponse cancelOrder(
        Long orderId,
        Long authenticatedUserId
    ) {
        log.debug(
            "Cancelling order {} for user {}",
            orderId,
            authenticatedUserId
        );

        Order order = orderRepository
            .findByIdAndUserId(orderId, authenticatedUserId)
            .orElseThrow(() -> {
                // Check if order exists at all
                if (orderRepository.existsById(orderId)) {
                    log.warn(
                        "User {} attempted to access order {} owned by another user",
                        authenticatedUserId,
                        orderId
                    );
                    throw new AccessDeniedException(
                        "You do not have permission to access this order"
                    );
                } else {
                    log.warn("Order {} not found", orderId);
                    throw new IllegalArgumentException(
                        "Order not found: " + orderId
                    );
                }
            });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        publishRefundEvent(order);
        publishInventoryRollback(order);
        publishEmailEvent(order);

        return OrderMapper.toOrderDetailResponse(order);
    }

    /**
     * Get total order count for a user.
     *
     * @param userId the user ID
     * @return total number of orders
     */
    @Transactional(readOnly = true)
    public long getUserOrderCount(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    private void publishRefundEvent(Order order) {
        eventPublisher.publishPaymentRefundEvent(
            PaymentRefundEvent.builder()
                .orderId(order.getId())
                .reason("Order cancel")
                .userId(order.getUser().getId())
                .build()
        );
    }

    private void publishInventoryRollback(Order order) {
        eventPublisher.publishInventoryRollbackEvent(
            InventoryRollbackEvent.builder()
                .orderId(order.getId())
                .productId(order.getProduct().getId())
                .amount(order.getQuantity())
                .reason("Order cancel")
                .build()
        );
    }

    private void publishEmailEvent(Order order) {
        EmailEvent emailEvent = EmailEvent.builder()
            .type("ORDER_CANCELLED")
            .to(order.getEmail())
            .template("order_cancelled")
            .params(
                Map.of(
                    "orderId",
                    "ORD-" + order.getId(),
                    "customerName",
                    order.getFirstName() + " " + order.getLastName(),
                    "processedDate",
                    LocalDateTime.now().toString()
                )
            )
            .eventId("evt-order-cancelled-" + order.getId())
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEmailEvent(emailEvent);
    }

    /**
     * Reserve stock with exponential backoff retry logic.
     *
     * Retry Strategy:
     * 1. Max retries: 3
     * 2. Backoff: 50ms, 100ms, 200ms (exponential)
     * 3. Retryable: UNAVAILABLE, DEADLINE_EXCEEDED, RESOURCE_EXHAUSTED
     * 4. Non-retryable: INVALID_ARGUMENT, NOT_FOUND, PERMISSION_DENIED<
     *
     * @param productId the product ID
     * @param quantity  the quantity to reserve
     * @param orderId   the order ID
     * @return ReserveStockResponse on success
     * @throws StatusRuntimeException if all retries fail or non-retryable error
     */
    private ReserveStockResponse reserveStockWithRetry(
        Long productId,
        Integer quantity,
        Long orderId
    ) {
        int attempt = 0;
        StatusRuntimeException lastException = null;

        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                log.debug(
                    "Reserve stock attempt {}/{} for order {} product {} quantity {}",
                    attempt,
                    MAX_RETRIES,
                    orderId,
                    productId,
                    quantity
                );

                ReserveStockResponse response = warehouseClient.reserveStock(
                    productId,
                    quantity,
                    orderId
                );

                if (attempt > 1) {
                    log.info(
                        "Reserve stock succeeded on attempt {} for order {}",
                        attempt,
                        orderId
                    );
                }

                return response;
            } catch (StatusRuntimeException e) {
                lastException = e;

                // Classify error as retryable or non-retryable
                boolean isRetryable = isRetryableError(e);

                log.warn(
                    "Reserve stock attempt {}/{} failed for order {}: {} (retryable: {})",
                    attempt,
                    MAX_RETRIES,
                    orderId,
                    e.getStatus(),
                    isRetryable
                );

                // Non-retryable errors: fail immediately
                if (!isRetryable) {
                    log.error(
                        "Non-retryable error for order {}: {}",
                        orderId,
                        e.getStatus()
                    );
                    throw e;
                }

                // If this was the last attempt, throw the exception
                if (attempt >= MAX_RETRIES) {
                    log.error(
                        "All {} retry attempts exhausted for order {}",
                        MAX_RETRIES,
                        orderId
                    );
                    throw e;
                }

                // Exponential backoff: 50ms, 100ms, 200ms
                long backoffMs = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
                log.debug(
                    "Retrying after {}ms backoff for order {}",
                    backoffMs,
                    orderId
                );

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error(
                        "Retry interrupted for order {}: {}",
                        orderId,
                        ie.getMessage()
                    );
                    throw new IllegalStateException(
                        "Stock reservation interrupted",
                        ie
                    );
                }
            }
        }

        // This should never be reached due to throw in loop, but satisfy compiler
        throw lastException;
    }

    /**
     * Determine if a gRPC error is retryable.
     *
     * Retryable errors (transient):
     * UNAVAILABLE: Service temporarily unavailable
     * DEADLINE_EXCEEDED: Request timeout
     * RESOURCE_EXHAUSTED: Rate limit or capacity issue
     *
     * Non-retryable errors (permanent):
     * INVALID_ARGUMENT: Bad request data
     * NOT_FOUND: Product doesn't exist
     * PERMISSION_DENIED: Authorization failure
     * FAILED_PRECONDITION: Business logic violation (insufficient stock)
     *
     * @param e the StatusRuntimeException
     * @return true if error is retryable, false otherwise
     */
    private boolean isRetryableError(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case UNAVAILABLE, DEADLINE_EXCEEDED, RESOURCE_EXHAUSTED -> true;
            case
                INVALID_ARGUMENT,
                NOT_FOUND,
                PERMISSION_DENIED,
                FAILED_PRECONDITION -> false;
            default -> false; // Conservative: don't retry unknown errors
        };
    }
}
