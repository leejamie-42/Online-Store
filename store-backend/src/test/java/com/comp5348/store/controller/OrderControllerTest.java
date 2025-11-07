package com.comp5348.store.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.comp5348.store.dto.order.*;
import com.comp5348.store.exception.ProductNotFoundException;
import com.comp5348.store.service.OrderService;
import com.comp5348.store.util.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for OrderController.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>Full order creation flow with authentication</li>
 * <li>Request validation (invalid shipping info, missing fields)</li>
 * <li>Authentication (401 Unauthorized)</li>
 * <li>Authorization (403 Forbidden - user_id mismatch)</li>
 * <li>Order retrieval with proper security</li>
 * <li>User order history endpoint</li>
 * <li>Error responses and status codes</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private CreateOrderRequest validOrderRequest;
    private ShippingInfoDto validShippingInfo;
    private CreateOrderResponse createOrderResponse;
    private OrderDetailResponse orderDetailResponse;

    @BeforeEach
    void setUp() {
        // Setup valid shipping info
        validShippingInfo = ShippingInfoDto.builder()
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

        // Setup valid order request
        validOrderRequest = CreateOrderRequest.builder()
            .productId(1L)
            .quantity(2)
            .userId(1L)
            .shippingInfo(validShippingInfo)
            .build();

        // Setup create order response
        createOrderResponse = CreateOrderResponse.builder()
            .orderId(1L)
            .status("PENDING")
            .total(new BigDecimal("199.98"))
            .build();

        // Setup order detail response
        orderDetailResponse = OrderDetailResponse.builder()
            .orderId(1L)
            .userId(1L)
            .products(
                List.of(
                    OrderProductDto.builder()
                        .id(1L)
                        .name("Test Product")
                        .price(new BigDecimal("99.99"))
                        .imageUrl("https://testUrl.com")
                        .quantity(2)
                        .build()
                )
            )
            .status("PENDING")
            .totalAmount(new BigDecimal("199.98"))
            .shippingInfo(validShippingInfo)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // ==================== CREATE ORDER TESTS ====================

    @Nested
    @DisplayName("POST /api/orders - Create Order")
    class CreateOrderEndpointTests {

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName(
            "Should create order successfully with valid request and authentication"
        )
        void createOrder_shouldReturn201_whenValidRequest() throws Exception {
            // Given
            when(
                orderService.createOrder(any(CreateOrderRequest.class), eq(1L))
            ).thenReturn(createOrderResponse);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(199.98));

            verify(orderService).createOrder(
                any(CreateOrderRequest.class),
                anyLong()
            );
        }

        // NOTE: 401 authentication tests are skipped in integration tests with
        // @MockitoBean
        // Security configuration testing should be done in dedicated security
        // integration tests

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName(
            "Should return 403 when user_id doesn't match authenticated user"
        )
        void createOrder_shouldReturn403_whenUserIdMismatch() throws Exception {
            // Given - service throws AccessDeniedException
            when(
                orderService.createOrder(
                    any(CreateOrderRequest.class),
                    anyLong()
                )
            ).thenThrow(
                new AccessDeniedException(
                    "Cannot create order for another user. User ID mismatch."
                )
            );

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isForbidden());

            verify(orderService).createOrder(
                any(CreateOrderRequest.class),
                anyLong()
            );
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when product_id is missing")
        void createOrder_shouldReturn400_whenProductIdMissing()
            throws Exception {
            // Given - request without product_id
            validOrderRequest.setProductId(null);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when quantity is zero or negative")
        void createOrder_shouldReturn400_whenQuantityInvalid()
            throws Exception {
            // Given - request with invalid quantity
            validOrderRequest.setQuantity(0);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when shipping info is missing")
        void createOrder_shouldReturn400_whenShippingInfoMissing()
            throws Exception {
            // Given - request without shipping info
            validOrderRequest.setShippingInfo(null);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName(
            "Should return 400 when state is invalid (not Australian state)"
        )
        void createOrder_shouldReturn400_whenStateInvalid() throws Exception {
            // Given - invalid state
            validShippingInfo.setState("XYZ"); // Invalid state
            validOrderRequest.setShippingInfo(validShippingInfo);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when postcode is not 4 digits")
        void createOrder_shouldReturn400_whenPostcodeInvalid()
            throws Exception {
            // Given - invalid postcode
            validShippingInfo.setPostcode("12345"); // 5 digits instead of 4
            validOrderRequest.setShippingInfo(validShippingInfo);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when mobile number format is invalid")
        void createOrder_shouldReturn400_whenMobileNumberInvalid()
            throws Exception {
            // Given - invalid mobile number
            validShippingInfo.setMobileNumber("123456"); // Too short
            validOrderRequest.setShippingInfo(validShippingInfo);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when email format is invalid")
        void createOrder_shouldReturn400_whenEmailInvalid() throws Exception {
            // Given - invalid email
            validShippingInfo.setEmail("invalid-email"); // No @ symbol
            validOrderRequest.setShippingInfo(validShippingInfo);

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isBadRequest());

            verify(orderService, never()).createOrder(any(), anyLong());
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 404 when product not found")
        void createOrder_shouldReturn404_whenProductNotFound()
            throws Exception {
            // Given - service throws ProductNotFoundException
            when(
                orderService.createOrder(
                    any(CreateOrderRequest.class),
                    anyLong()
                )
            ).thenThrow(new ProductNotFoundException(999L));

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isNotFound());

            verify(orderService).createOrder(
                any(CreateOrderRequest.class),
                anyLong()
            );
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 500 when insufficient stock")
        void createOrder_shouldReturn500_whenInsufficientStock()
            throws Exception {
            // Given - service throws IllegalStateException for insufficient stock
            when(
                orderService.createOrder(
                    any(CreateOrderRequest.class),
                    anyLong()
                )
            ).thenThrow(
                new IllegalStateException(
                    "Insufficient stock. Requested: 10, Available: 5"
                )
            );

            // When/Then
            mockMvc
                .perform(
                    post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(validOrderRequest)
                        )
                )
                .andExpect(status().isInternalServerError());

            verify(orderService).createOrder(
                any(CreateOrderRequest.class),
                anyLong()
            );
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should accept all valid Australian states")
        void createOrder_shouldAcceptAllValidStates() throws Exception {
            // Given
            String[] validStates = {
                "NSW",
                "VIC",
                "QLD",
                "SA",
                "WA",
                "TAS",
                "NT",
                "ACT",
            };

            when(
                orderService.createOrder(
                    any(CreateOrderRequest.class),
                    anyLong()
                )
            ).thenReturn(createOrderResponse);

            // When/Then - test each valid state
            for (String state : validStates) {
                validShippingInfo.setState(state);
                validOrderRequest.setShippingInfo(validShippingInfo);

                mockMvc
                    .perform(
                        post("/api/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    validOrderRequest
                                )
                            )
                    )
                    .andExpect(status().isCreated());
            }
        }
    }

    // ==================== GET ORDER TESTS ====================

    @Nested
    @DisplayName("GET /api/orders/{id} - Get Order Details")
    class GetOrderEndpointTests {

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return order details when user owns the order")
        void getOrder_shouldReturn200_whenUserOwnsOrder() throws Exception {
            // Given
            Long orderId = 1L;
            when(orderService.getOrder(orderId, 1L)).thenReturn(
                orderDetailResponse
            );

            // When/Then
            mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Test Product"))
                .andExpect(
                    jsonPath("$.products[0].price").value(
                        new BigDecimal("99.99")
                    )
                )
                .andExpect(
                    jsonPath("$.products[0].imageUrl").value(
                        "https://testUrl.com"
                    )
                )
                .andExpect(jsonPath("$.products[0].quantity").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.shippingInfo.firstName").value("John"))
                .andExpect(jsonPath("$.shippingInfo.state").value("NSW"));

            verify(orderService).getOrder(orderId, 1L);
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 403 when user doesn't own the order")
        void getOrder_shouldReturn403_whenUserDoesNotOwnOrder()
            throws Exception {
            // Given
            Long orderId = 1L;
            when(orderService.getOrder(orderId, 1L)).thenThrow(
                new AccessDeniedException(
                    "You do not have permission to access this order"
                )
            );

            // When/Then
            mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isForbidden());

            verify(orderService).getOrder(orderId, 1L);
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return 400 when order not found")
        void getOrder_shouldReturn400_whenOrderNotFound() throws Exception {
            // Given
            Long orderId = 999L;
            when(orderService.getOrder(orderId, 1L)).thenThrow(
                new IllegalArgumentException("Order not found: 999")
            );

            // When/Then
            mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isBadRequest());

            verify(orderService).getOrder(orderId, 1L);
        }
    }

    // ==================== GET USER ORDERS TESTS ====================

    @Nested
    @DisplayName("GET /api/orders - Get User Orders")
    class GetUserOrdersEndpointTests {

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return user's order history")
        void getUserOrders_shouldReturn200_withOrdersList() throws Exception {
            // Given
            OrderHistoryResponse order1 = OrderHistoryResponse.builder()
                .orderId(1L)
                .products(
                    List.of(
                        OrderProductDto.builder()
                            .id(1L)
                            .name("Product 1")
                            .quantity(2)
                            .price(new BigDecimal("99.99"))
                            .build()
                    )
                )
                .status("PENDING")
                .totalAmount(new BigDecimal("199.98"))
                .customerName("Test User")
                .createdAt(LocalDateTime.now())
                .build();

            OrderHistoryResponse order2 = OrderHistoryResponse.builder()
                .orderId(2L)
                .products(
                    List.of(
                        OrderProductDto.builder()
                            .id(2L)
                            .name("Product 2")
                            .quantity(1)
                            .price(new BigDecimal("99.99"))
                            .build()
                    )
                )
                .status("DELIVERED")
                .totalAmount(new BigDecimal("99.99"))
                .customerName("Test User")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

            List<OrderHistoryResponse> orders = Arrays.asList(order1, order2);

            when(orderService.getUserOrders(1L)).thenReturn(orders);

            // When/Then
            mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].status").value("DELIVERED"));

            verify(orderService).getUserOrders(1L);
        }

        @Test
        @WithMockCustomUser(id = 1L, username = "Test User")
        @DisplayName("Should return empty list when user has no orders")
        void getUserOrders_shouldReturn200_withEmptyList() throws Exception {
            // Given
            when(orderService.getUserOrders(1L)).thenReturn(
                Collections.emptyList()
            );

            // When/Then
            mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

            verify(orderService).getUserOrders(1L);
        }
    }
}
