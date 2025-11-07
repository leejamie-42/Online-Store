package com.comp5348.store.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.comp5348.store.dto.order.*;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.grpc.warehouse.CheckStockResponse;
import com.comp5348.store.grpc.warehouse.ReserveStockResponse;
import com.comp5348.store.model.Product;
import com.comp5348.store.model.auth.User;
import com.comp5348.store.model.auth.UserRole;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import com.comp5348.store.repository.OrderRepository;
import com.comp5348.store.repository.ProductRepository;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.service.warehouse.WarehouseGrpcClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit tests for OrderService.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>Successful order creation with stock validation and reservation</li>
 * <li>User authorization (user_id mismatch scenarios)</li>
 * <li>Product not found exception handling</li>
 * <li>Insufficient stock scenarios</li>
 * <li>Stock rollback on database/reservation failures</li>
 * <li>Order retrieval with authorization checks</li>
 * <li>User order history retrieval</li>
 * <li>gRPC retry logic and error handling</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WarehouseGrpcClient warehouseClient;

    @InjectMocks
    private OrderService orderService;

    // Test data
    private User testUser;
    private Product testProduct;
    private CreateOrderRequest createOrderRequest;
    private ShippingInfoDto shippingInfo;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .password("hashedPassword")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();

        // Setup test product
        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .imageUrl("https://example.com/image.jpg")
            .quantity(100)
            .build();

        // Setup shipping info
        shippingInfo = ShippingInfoDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .mobileNumber("0400000000")
            .addressLine1("123 Main St")
            .city("Sydney")
            .state("NSW")
            .postcode("2000")
            .country("Australia")
            .build();

        // Setup create order request
        createOrderRequest = CreateOrderRequest.builder()
            .productId(1L)
            .quantity(2)
            .userId(1L)
            .shippingInfo(shippingInfo)
            .build();

        // Setup test order
        testOrder = Order.builder()
            .id(1L)
            .user(testUser)
            .product(testProduct)
            .quantity(2)
            .status(OrderStatus.PENDING)
            .totalAmount(new BigDecimal("199.98"))
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .mobileNumber("0400000000")
            .addressLine1("123 Main St")
            .city("Sydney")
            .state("NSW")
            .postcode("2000")
            .country("Australia")
            .build();
    }

    // ==================== CREATE ORDER TESTS ====================

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with valid request")
        void createOrder_shouldSucceed_whenAllValidationsPassed() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock warehouse stock check - available
            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(true)
                    .setTotalAvailable(50)
                    .build();
            when(warehouseClient.checkStock(1L, 2)).thenReturn(
                stockCheckResponse
            );

            // Mock order save (to generate ID)
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Mock warehouse stock reservation - success
            ReserveStockResponse reserveResponse =
                ReserveStockResponse.newBuilder()
                    .setSuccess(true)
                    .addReservedFromWarehouses("warehouse-1")
                    .setMessage("Stock reserved successfully")
                    .build();
            when(warehouseClient.reserveStock(1L, 2, 1L)).thenReturn(
                reserveResponse
            );

            // When
            CreateOrderResponse response = orderService.createOrder(
                createOrderRequest,
                1L
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getTotal()).isEqualByComparingTo(
                new BigDecimal("199.98")
            );

            // Verify interactions
            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(warehouseClient).checkStock(1L, 2);
            verify(orderRepository).save(any(Order.class));
            verify(warehouseClient).reserveStock(1L, 2, 1L);

            // Verify order entity was constructed correctly
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(
                Order.class
            );
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getUser()).isEqualTo(testUser);
            assertThat(savedOrder.getProduct()).isEqualTo(testProduct);
            assertThat(savedOrder.getQuantity()).isEqualTo(2);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(savedOrder.getFirstName()).isEqualTo("John");
            assertThat(savedOrder.getLastName()).isEqualTo("Doe");
            assertThat(savedOrder.getState()).isEqualTo("NSW");
            assertThat(savedOrder.getPostcode()).isEqualTo("2000");
        }

        @Test
        @DisplayName(
            "Should throw AccessDeniedException when user_id doesn't match authenticated user"
        )
        void createOrder_shouldThrowAccessDenied_whenUserIdMismatch() {
            // Given - request has userId=1 but authenticated user is 2
            Long authenticatedUserId = 2L;

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(
                    createOrderRequest,
                    authenticatedUserId
                )
            )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cannot create order for another user");

            // Verify no downstream calls were made
            verify(userRepository, never()).findById(anyLong());
            verify(productRepository, never()).findById(anyLong());
            verify(warehouseClient, never()).checkStock(anyLong(), anyInt());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName(
            "Should throw IllegalArgumentException when user not found"
        )
        void createOrder_shouldThrowException_whenUserNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found: 1");

            verify(userRepository).findById(1L);
            verify(productRepository, never()).findById(anyLong());
            verify(warehouseClient, never()).checkStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName(
            "Should throw ProductNotFoundException when product not found"
        )
        void createOrder_shouldThrowProductNotFound_whenProductDoesNotExist() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            ).isInstanceOf(ProductNotFoundException.class);

            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(warehouseClient, never()).checkStock(anyLong(), anyInt());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName(
            "Should throw IllegalStateException when stock check fails with gRPC error"
        )
        void createOrder_shouldThrowException_whenStockCheckGrpcFails() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock gRPC failure
            StatusRuntimeException grpcException = new StatusRuntimeException(
                Status.UNAVAILABLE.withDescription(
                    "Warehouse service unavailable"
                )
            );
            when(warehouseClient.checkStock(1L, 2)).thenThrow(grpcException);

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to check stock availability");

            verify(warehouseClient).checkStock(1L, 2);
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName(
            "Should throw IllegalStateException when insufficient stock"
        )
        void createOrder_shouldThrowException_whenInsufficientStock() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock warehouse stock check - insufficient
            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(false)
                    .setTotalAvailable(1) // Only 1 available, but requested 2
                    .build();
            when(warehouseClient.checkStock(1L, 2)).thenReturn(
                stockCheckResponse
            );

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("Requested: 2")
                .hasMessageContaining("Available: 1");

            verify(warehouseClient).checkStock(1L, 2);
            verify(orderRepository, never()).save(any());
            verify(warehouseClient, never()).reserveStock(
                anyLong(),
                anyInt(),
                anyLong()
            );
        }

        @Test
        @DisplayName("Should rollback order when stock reservation fails")
        void createOrder_shouldRollbackOrder_whenStockReservationFails() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock warehouse stock check - available
            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(true)
                    .setTotalAvailable(50)
                    .build();
            when(warehouseClient.checkStock(1L, 2)).thenReturn(
                stockCheckResponse
            );

            // Mock order save (to generate ID)
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Mock warehouse stock reservation - failure
            ReserveStockResponse reserveResponse =
                ReserveStockResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Reservation failed - concurrent update")
                    .build();
            when(warehouseClient.reserveStock(1L, 2, 1L)).thenReturn(
                reserveResponse
            );

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock reservation failed");

            // Verify order was created then deleted (rollback)
            verify(orderRepository).save(any(Order.class));
            verify(orderRepository).delete(testOrder);
        }

        @Test
        @DisplayName(
            "Should rollback order when gRPC reservation throws exception"
        )
        void createOrder_shouldRollbackOrder_whenGrpcReservationThrowsException() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock warehouse stock check - available
            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(true)
                    .setTotalAvailable(50)
                    .build();
            when(warehouseClient.checkStock(1L, 2)).thenReturn(
                stockCheckResponse
            );

            // Mock order save (to generate ID)
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Mock gRPC reservation failure
            StatusRuntimeException grpcException = new StatusRuntimeException(
                Status.DEADLINE_EXCEEDED.withDescription("Timeout")
            );
            when(warehouseClient.reserveStock(1L, 2, 1L)).thenThrow(
                grpcException
            );

            // When/Then
            assertThatThrownBy(() ->
                orderService.createOrder(createOrderRequest, 1L)
            )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to reserve stock");

            // Verify order was created then deleted (rollback)
            verify(orderRepository).save(any(Order.class));
            verify(orderRepository).delete(testOrder);
        }

        @Test
        @DisplayName("Should calculate total amount correctly")
        void createOrder_shouldCalculateTotalCorrectly() {
            // Given - product price $99.99, quantity 3
            createOrderRequest.setQuantity(3);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(true)
                    .setTotalAvailable(50)
                    .build();
            when(warehouseClient.checkStock(1L, 3)).thenReturn(
                stockCheckResponse
            );

            Order orderWithTotal = Order.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(3)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("299.97")) // 99.99 * 3
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .mobileNumber("0400000000")
                .addressLine1("123 Main St")
                .city("Sydney")
                .state("NSW")
                .postcode("2000")
                .country("Australia")
                .build();

            when(orderRepository.save(any(Order.class))).thenReturn(
                orderWithTotal
            );

            ReserveStockResponse reserveResponse =
                ReserveStockResponse.newBuilder()
                    .setSuccess(true)
                    .addReservedFromWarehouses("warehouse-1")
                    .setMessage("Stock reserved successfully")
                    .build();
            when(warehouseClient.reserveStock(1L, 3, 1L)).thenReturn(
                reserveResponse
            );

            // When
            CreateOrderResponse response = orderService.createOrder(
                createOrderRequest,
                1L
            );

            // Then
            assertThat(response.getTotal()).isEqualByComparingTo(
                new BigDecimal("299.97")
            );

            // Verify saved order had correct total
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(
                Order.class
            );
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(
                new BigDecimal("299.97")
            );
        }

        @Test
        @DisplayName(
            "Should create order successfully with multi-warehouse stock reservation"
        )
        void createOrder_shouldSucceed_whenStockReservedFromMultipleWarehouses() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(productRepository.findById(1L)).thenReturn(
                Optional.of(testProduct)
            );

            // Mock warehouse stock check - available
            CheckStockResponse stockCheckResponse =
                CheckStockResponse.newBuilder()
                    .setAvailable(true)
                    .setTotalAvailable(50)
                    .build();
            when(warehouseClient.checkStock(1L, 2)).thenReturn(
                stockCheckResponse
            );

            // Mock order save (to generate ID)
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

            // Mock warehouse stock reservation - success from multiple warehouses
            ReserveStockResponse reserveResponse =
                ReserveStockResponse.newBuilder()
                    .setSuccess(true)
                    .addReservedFromWarehouses("warehouse-1")
                    .addReservedFromWarehouses("warehouse-2")
                    .setMessage(
                        "Stock reserved successfully from multiple warehouses"
                    )
                    .build();
            when(warehouseClient.reserveStock(1L, 2, 1L)).thenReturn(
                reserveResponse
            );

            // When
            CreateOrderResponse response = orderService.createOrder(
                createOrderRequest,
                1L
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getTotal()).isEqualByComparingTo(
                new BigDecimal("199.98")
            );

            // Verify interactions
            verify(userRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(warehouseClient).checkStock(1L, 2);
            verify(orderRepository).save(any(Order.class));
            verify(warehouseClient).reserveStock(1L, 2, 1L);
        }
    }

    // ==================== GET ORDER TESTS ====================

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order details when user owns the order")
        void getOrder_shouldReturnOrderDetails_whenUserOwnsOrder() {
            // Given
            Long orderId = 1L;
            Long authenticatedUserId = 1L;

            when(
                orderRepository.findByIdAndUserId(orderId, authenticatedUserId)
            ).thenReturn(Optional.of(testOrder));

            // When
            OrderDetailResponse response = orderService.getOrder(
                orderId,
                authenticatedUserId
            );

            OrderProductDto orderProdcut = response.getProducts().get(0);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(orderProdcut.getId()).isEqualTo(1L);
            assertThat(orderProdcut.getName()).isEqualTo("Test Product");
            assertThat(orderProdcut.getQuantity()).isEqualTo(2);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(
                new BigDecimal("199.98")
            );
            assertThat(response.getShippingInfo()).isNotNull();
            assertThat(response.getShippingInfo().getFirstName()).isEqualTo(
                "John"
            );
            assertThat(response.getShippingInfo().getState()).isEqualTo("NSW");

            verify(orderRepository).findByIdAndUserId(
                orderId,
                authenticatedUserId
            );
        }

        @Test
        @DisplayName(
            "Should throw AccessDeniedException when user doesn't own the order"
        )
        void getOrder_shouldThrowAccessDenied_whenUserDoesNotOwnOrder() {
            // Given
            Long orderId = 1L;
            Long authenticatedUserId = 2L; // Different user

            when(
                orderRepository.findByIdAndUserId(orderId, authenticatedUserId)
            ).thenReturn(Optional.empty());
            when(orderRepository.existsById(orderId)).thenReturn(true); // Order exists but belongs to another user

            // When/Then
            assertThatThrownBy(() ->
                orderService.getOrder(orderId, authenticatedUserId)
            )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(
                    "You do not have permission to access this order"
                );

            verify(orderRepository).findByIdAndUserId(
                orderId,
                authenticatedUserId
            );
            verify(orderRepository).existsById(orderId);
        }

        @Test
        @DisplayName(
            "Should throw IllegalArgumentException when order doesn't exist"
        )
        void getOrder_shouldThrowException_whenOrderDoesNotExist() {
            // Given
            Long orderId = 999L;
            Long authenticatedUserId = 1L;

            when(
                orderRepository.findByIdAndUserId(orderId, authenticatedUserId)
            ).thenReturn(Optional.empty());
            when(orderRepository.existsById(orderId)).thenReturn(false); // Order doesn't exist

            // When/Then
            assertThatThrownBy(() ->
                orderService.getOrder(orderId, authenticatedUserId)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found: 999");

            verify(orderRepository).findByIdAndUserId(
                orderId,
                authenticatedUserId
            );
            verify(orderRepository).existsById(orderId);
        }
    }

    // ==================== GET USER ORDERS TESTS ====================

    @Nested
    @DisplayName("Get User Orders Tests")
    class GetUserOrdersTests {

        @Test
        @DisplayName(
            "Should return all orders for user in descending date order"
        )
        void getUserOrders_shouldReturnOrdersList_whenUserHasOrders() {
            // Given
            Long authenticatedUserId = 1L;

            Order order1 = Order.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98"))
                .build();

            Order order2 = Order.builder()
                .id(2L)
                .user(testUser)
                .product(testProduct)
                .quantity(1)
                .status(OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("99.99"))
                .build();

            List<Order> orders = Arrays.asList(order2, order1); // Newest first

            when(
                orderRepository.findByUserIdOrderByCreatedAtDesc(
                    authenticatedUserId
                )
            ).thenReturn(orders);

            // When
            List<OrderHistoryResponse> response = orderService.getUserOrders(
                authenticatedUserId
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(2);
            assertThat(response.get(0).getOrderId()).isEqualTo(2L);
            assertThat(response.get(0).getStatus()).isEqualTo("DELIVERED");
            assertThat(response.get(1).getOrderId()).isEqualTo(1L);
            assertThat(response.get(1).getStatus()).isEqualTo("PENDING");

            verify(orderRepository).findByUserIdOrderByCreatedAtDesc(
                authenticatedUserId
            );
        }

        @Test
        @DisplayName("Should return empty list when user has no orders")
        void getUserOrders_shouldReturnEmptyList_whenUserHasNoOrders() {
            // Given
            Long authenticatedUserId = 1L;
            when(
                orderRepository.findByUserIdOrderByCreatedAtDesc(
                    authenticatedUserId
                )
            ).thenReturn(Collections.emptyList());

            // When
            List<OrderHistoryResponse> response = orderService.getUserOrders(
                authenticatedUserId
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();

            verify(orderRepository).findByUserIdOrderByCreatedAtDesc(
                authenticatedUserId
            );
        }
    }

    // ==================== GET USER ORDER COUNT TESTS ====================

    @Nested
    @DisplayName("Get User Order Count Tests")
    class GetUserOrderCountTests {

        @Test
        @DisplayName("Should return correct order count for user")
        void getUserOrderCount_shouldReturnCount() {
            // Given
            Long userId = 1L;
            when(orderRepository.countByUserId(userId)).thenReturn(5L);

            // When
            long count = orderService.getUserOrderCount(userId);

            // Then
            assertThat(count).isEqualTo(5L);
            verify(orderRepository).countByUserId(userId);
        }

        @Test
        @DisplayName("Should return zero when user has no orders")
        void getUserOrderCount_shouldReturnZero_whenNoOrders() {
            // Given
            Long userId = 1L;
            when(orderRepository.countByUserId(userId)).thenReturn(0L);

            // When
            long count = orderService.getUserOrderCount(userId);

            // Then
            assertThat(count).isEqualTo(0L);
            verify(orderRepository).countByUserId(userId);
        }
    }
}
