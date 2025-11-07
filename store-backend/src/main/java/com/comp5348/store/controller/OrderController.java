package com.comp5348.store.controller;

import com.comp5348.store.dto.order.CreateOrderRequest;
import com.comp5348.store.dto.order.CreateOrderResponse;
import com.comp5348.store.dto.order.OrderDetailResponse;
import com.comp5348.store.dto.order.OrderHistoryResponse;
import com.comp5348.store.model.auth.User;
import com.comp5348.store.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order management operations.
 *
 * All endpoints require authentication via JWT token in Authorization header.
 * Users can only access their own orders (enforced at service layer).
 */
@Tag(name = "Order", description = "Order management operations")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order.
     *
     * Workflow:
     * Validate user authorization (request.userId must match authenticated user)
     * Check stock availability via Warehouse gRPC
     * Reserve stock atomically via Warehouse gRPC
     * Create order with PENDING status
     * Return order details with total amount
     *
     * @param request           the order creation request with product, quantity,
     *                          and shipping info
     * @param authenticatedUser the authenticated user from JWT token
     * @return CreateOrderResponse with order ID, status, and total amount
     * @throws org.springframework.security.access.AccessDeniedException if user_id
     *                                                                   mismatch
     * @throws com.comp5348.store.exception.ProductNotFoundException     if product
     *                                                                   not found
     * @throws com.comp5348.store.exception.InsufficientStockException   if stock
     *                                                                   unavailable
     */
    @Operation(summary = "Create a new order", description = "Create a new order with stock validation and reservation. Requires authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (validation errors)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user_id doesn't match authenticated user"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock to fulfill order"),
            @ApiResponse(responseCode = "500", description = "Internal server error (warehouse communication failure)"),
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        log.info(
                "POST /api/orders - Create order request for user {} product {} quantity {}",
                request.getUserId(),
                request.getProductId(),
                request.getQuantity());

        CreateOrderResponse response = orderService.createOrder(
                request,
                authenticatedUser.getId());

        log.info("Order created successfully: {}", response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order details by ID.
     *
     * <p>
     * Users can only access their own orders. Attempting to access another user's
     * order returns 403 Forbidden.
     *
     * @param orderId           the order ID
     * @param authenticatedUser the authenticated user from JWT token
     * @return OrderDetailResponse with complete order information
     * @throws com.comp5348.store.exception.OrderNotFoundException       if order
     *                                                                   not found
     * @throws org.springframework.security.access.AccessDeniedException if order
     *                                                                   belongs to
     *                                                                   another
     *                                                                   user
     */
    @Operation(summary = "Get order details", description = "Retrieve complete order details including product, shipping, and status. Users can only access their own orders.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - order belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        log.info(
                "GET /api/orders/{} - Get order details for user {}",
                id,
                authenticatedUser.getId());

        OrderDetailResponse response = orderService.getOrder(
                id,
                authenticatedUser.getId());

        log.info("Returning order {} with status {}", id, response.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for the authenticated user.
     *
     * <p>
     * Returns orders in descending order by creation date (newest first).
     * Returns empty list if user has no orders.
     *
     * @param authenticatedUser the authenticated user from JWT token
     * @return List of OrderSummaryResponse ordered by creation date descending
     */
    @Operation(summary = "Get user's order history", description = "Retrieve all orders for the authenticated user, sorted by creation date (newest first).", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully (may be empty list)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
    })
    @GetMapping
    public ResponseEntity<List<OrderHistoryResponse>> getUserOrders(
            @AuthenticationPrincipal User authenticatedUser) {
        log.info(
                "GET /api/orders - Get orders for user {}",
                authenticatedUser.getId());

        List<OrderHistoryResponse> orders = orderService.getUserOrders(
                authenticatedUser.getId());

        log.info(
                "Returning {} orders for user {}",
                orders.size(),
                authenticatedUser.getId());
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel order by ID.
     *
     * @param orderId           the order ID
     * @param authenticatedUser the authenticated user from JWT token
     * @return OrderCancelResponse with complete order information
     * @throws com.comp5348.store.exception.OrderNotFoundException       if order
     *                                                                   not found
     * @throws org.springframework.security.access.AccessDeniedException if order
     *                                                                   belongs to
     *                                                                   another
     *                                                                   user
     */
    @Operation(summary = "Cancel order", description = "Cancel order and process payment refund and inventory rollback.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancel order success"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - order belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDetailResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        log.info(
                "PUT /api/orders/{}/cancel - Cancel order for user {}",
                id,
                authenticatedUser.getId());

        OrderDetailResponse response = orderService.cancelOrder(
                id,
                authenticatedUser.getId());

        log.info("Returning order {} with status {}", id, response.getStatus());
        return ResponseEntity.ok(response);
    }
}
