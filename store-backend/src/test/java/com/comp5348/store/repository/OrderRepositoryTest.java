package com.comp5348.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.comp5348.store.model.Product;
import com.comp5348.store.model.auth.User;
import com.comp5348.store.model.auth.UserRole;
import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository tests for OrderRepository.
 *
 * <p>
 * Uses @DataJpaTest for lightweight JPA testing with H2 in-memory database.
 * </p>
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>Basic CRUD operations</li>
 * <li>Custom query methods (findByUserId, findByIdAndUserId, etc.)</li>
 * <li>Order by clauses (createdAt DESC)</li>
 * <li>Count and exists queries</li>
 * <li>Timestamp auto-population (@PrePersist)</li>
 * </ul>
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProductRepository productRepository;

  private User testUser;
  private User otherUser;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    // Clean up
    orderRepository.deleteAll();
    userRepository.deleteAll();
    productRepository.deleteAll();

    // Create test users
    testUser = User.builder()
        .name("Test User")
        .email("test@example.com")
        .password("hashedPassword")
        .role(UserRole.CUSTOMER)
        .enabled(true)
        .build();
    testUser = userRepository.save(testUser);

    otherUser = User.builder()
        .name("Other User")
        .email("other@example.com")
        .password("hashedPassword")
        .role(UserRole.CUSTOMER)
        .enabled(true)
        .build();
    otherUser = userRepository.save(otherUser);

    // Create test product
    testProduct = Product.builder()
        .name("Test Product")
        .description("Test Description")
        .price(new BigDecimal("99.99"))
        .imageUrl("https://example.com/image.jpg")
        .quantity(100)
        .build();
    testProduct = productRepository.save(testProduct);
  }

  @Test
  @DisplayName("Should save order with all fields")
  void whenSaveOrder_thenOrderIsPersisted() {
    // Given
    Order order = Order.builder()
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

    // When
    Order savedOrder = orderRepository.save(order);

    // Then
    assertThat(savedOrder).isNotNull();
    assertThat(savedOrder.getId()).isNotNull();
    assertThat(savedOrder.getUser()).isEqualTo(testUser);
    assertThat(savedOrder.getProduct()).isEqualTo(testProduct);
    assertThat(savedOrder.getQuantity()).isEqualTo(2);
    assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(
        new BigDecimal("199.98"));
    assertThat(savedOrder.getFirstName()).isEqualTo("John");
    assertThat(savedOrder.getLastName()).isEqualTo("Doe");
    assertThat(savedOrder.getState()).isEqualTo("NSW");
    assertThat(savedOrder.getPostcode()).isEqualTo("2000");
    assertThat(savedOrder.getCreatedAt()).isNotNull();
    assertThat(savedOrder.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should auto-populate timestamps on creation")
  void whenSaveOrder_thenTimestampsAreAutoPopulated() {
    // Given
    Order order = Order.builder()
        .user(testUser)
        .product(testProduct)
        .quantity(1)
        .status(OrderStatus.PENDING)
        .totalAmount(new BigDecimal("99.99"))
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

    // When
    Order savedOrder = orderRepository.save(order);

    // Then - timestamps should be auto-populated by @PrePersist
    assertThat(savedOrder.getCreatedAt()).isNotNull();
    assertThat(savedOrder.getUpdatedAt()).isNotNull();
    // Timestamps should be very close (within same second)
    assertThat(savedOrder.getCreatedAt()).isBeforeOrEqualTo(
        savedOrder.getUpdatedAt());
  }

  @Test
  @DisplayName("Should find order by ID")
  void whenFindById_thenReturnOrder() {
    // Given
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);

    // When
    Optional<Order> foundOrder = orderRepository.findById(order.getId());

    // Then
    assertThat(foundOrder).isPresent();
    assertThat(foundOrder.get().getId()).isEqualTo(order.getId());
    assertThat(foundOrder.get().getUser().getId()).isEqualTo(
        testUser.getId());
  }

  @Test
  @DisplayName("Should find orders by user ID ordered by creation date descending")
  void whenFindByUserIdOrderByCreatedAtDesc_thenReturnOrdersInCorrectOrder() {
    // Given - Create 3 orders for testUser
    Order order1 = createTestOrder(testUser, testProduct, 1);
    Order order2 = createTestOrder(testUser, testProduct, 2);
    Order order3 = createTestOrder(testUser, testProduct, 3);

    // Save in order (but we'll verify they're returned newest first)
    orderRepository.save(order1);
    orderRepository.save(order2);
    orderRepository.save(order3);

    // When
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
        testUser.getId());

    // Then
    assertThat(orders).hasSize(3);
    // Verify descending order (newest first)
    assertThat(orders.get(0).getQuantity()).isEqualTo(3); // Last created
    assertThat(orders.get(1).getQuantity()).isEqualTo(2);
    assertThat(orders.get(2).getQuantity()).isEqualTo(1); // First created
  }

  @Test
  @DisplayName("Should return empty list when user has no orders")
  void whenFindByUserIdOrderByCreatedAtDesc_thenReturnEmptyList_whenNoOrders() {
    // When
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
        testUser.getId());

    // Then
    assertThat(orders).isEmpty();
  }

  @Test
  @DisplayName("Should find order by ID and user ID")
  void whenFindByIdAndUserId_thenReturnOrder_whenUserOwnsOrder() {
    // Given
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);

    // When
    Optional<Order> foundOrder = orderRepository.findByIdAndUserId(
        order.getId(),
        testUser.getId());

    // Then
    assertThat(foundOrder).isPresent();
    assertThat(foundOrder.get().getId()).isEqualTo(order.getId());
    assertThat(foundOrder.get().getUser().getId()).isEqualTo(
        testUser.getId());
  }

  @Test
  @DisplayName("Should return empty when order exists but belongs to different user")
  void whenFindByIdAndUserId_thenReturnEmpty_whenOrderBelongsToOtherUser() {
    // Given - Order belongs to testUser
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);

    // When - Try to find with otherUser's ID
    Optional<Order> foundOrder = orderRepository.findByIdAndUserId(
        order.getId(),
        otherUser.getId());

    // Then
    assertThat(foundOrder).isEmpty();
  }

  @Test
  @DisplayName("Should count orders by user ID")
  void whenCountByUserId_thenReturnCorrectCount() {
    // Given - Create 3 orders for testUser and 2 for otherUser
    orderRepository.save(createTestOrder(testUser, testProduct, 1));
    orderRepository.save(createTestOrder(testUser, testProduct, 2));
    orderRepository.save(createTestOrder(testUser, testProduct, 3));
    orderRepository.save(createTestOrder(otherUser, testProduct, 1));
    orderRepository.save(createTestOrder(otherUser, testProduct, 2));

    // When
    long testUserOrderCount = orderRepository.countByUserId(
        testUser.getId());
    long otherUserOrderCount = orderRepository.countByUserId(
        otherUser.getId());

    // Then
    assertThat(testUserOrderCount).isEqualTo(3);
    assertThat(otherUserOrderCount).isEqualTo(2);
  }

  @Test
  @DisplayName("Should return zero count when user has no orders")
  void whenCountByUserId_thenReturnZero_whenNoOrders() {
    // When
    long count = orderRepository.countByUserId(testUser.getId());

    // Then
    assertThat(count).isEqualTo(0);
  }

  @Test
  @DisplayName("Should check if order exists by ID and user ID")
  void whenExistsByIdAndUserId_thenReturnTrue_whenOrderExists() {
    // Given
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);

    // When
    boolean exists = orderRepository.existsByIdAndUserId(
        order.getId(),
        testUser.getId());

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Should return false when order doesn't exist for user")
  void whenExistsByIdAndUserId_thenReturnFalse_whenOrderNotForUser() {
    // Given - Order belongs to testUser
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);

    // When - Check with otherUser's ID
    boolean exists = orderRepository.existsByIdAndUserId(
        order.getId(),
        otherUser.getId());

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Should find orders by status")
  void whenFindByStatus_thenReturnOrdersWithMatchingStatus() {
    // Given - Create orders with different statuses
    Order pendingOrder1 = createTestOrder(testUser, testProduct, 1);
    pendingOrder1.setStatus(OrderStatus.PENDING);
    orderRepository.save(pendingOrder1);

    Order pendingOrder2 = createTestOrder(testUser, testProduct, 2);
    pendingOrder2.setStatus(OrderStatus.PENDING);
    orderRepository.save(pendingOrder2);

    Order deliveredOrder = createTestOrder(otherUser, testProduct, 1);
    deliveredOrder.setStatus(OrderStatus.DELIVERED);
    orderRepository.save(deliveredOrder);

    // When
    List<Order> pendingOrders = orderRepository.findByStatus(
        OrderStatus.PENDING);
    List<Order> deliveredOrders = orderRepository.findByStatus(
        OrderStatus.DELIVERED);

    // Then
    assertThat(pendingOrders).hasSize(2);
    assertThat(deliveredOrders).hasSize(1);
    assertThat(pendingOrders).allMatch(order -> order.getStatus() == OrderStatus.PENDING);
    assertThat(deliveredOrders.get(0).getStatus()).isEqualTo(
        OrderStatus.DELIVERED);
  }

  @Test
  @DisplayName("Should delete order")
  void whenDeleteOrder_thenOrderIsRemoved() {
    // Given
    Order order = createTestOrder(testUser, testProduct, 2);
    order = orderRepository.save(order);
    Long orderId = order.getId();

    // When
    orderRepository.delete(order);

    // Then
    Optional<Order> deletedOrder = orderRepository.findById(orderId);
    assertThat(deletedOrder).isEmpty();
  }

  @Test
  @DisplayName("Should update order status")
  void whenUpdateOrderStatus_thenStatusIsChanged() {
    // Given
    Order order = createTestOrder(testUser, testProduct, 2);
    order.setStatus(OrderStatus.PENDING);
    order = orderRepository.save(order);

    // When
    order.setStatus(OrderStatus.PROCESSING);
    Order updatedOrder = orderRepository.save(order);

    // Then
    assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);

    // Verify in database
    Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();
    assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  @DisplayName("Should handle multiple orders for same user and product")
  void whenSaveMultipleOrdersSameUserAndProduct_thenAllAreStored() {
    // Given
    Order order1 = createTestOrder(testUser, testProduct, 1);
    Order order2 = createTestOrder(testUser, testProduct, 2);
    Order order3 = createTestOrder(testUser, testProduct, 3);

    // When
    orderRepository.save(order1);
    orderRepository.save(order2);
    orderRepository.save(order3);

    // Then
    List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
        testUser.getId());
    assertThat(orders).hasSize(3);
  }

  // ==================== HELPER METHODS ====================

  /**
   * Helper method to create a test order with minimal required fields.
   */
  private Order createTestOrder(User user, Product product, int quantity) {
    return Order.builder()
        .user(user)
        .product(product)
        .quantity(quantity)
        .status(OrderStatus.PENDING)
        .totalAmount(
            product.getPrice().multiply(BigDecimal.valueOf(quantity)))
        .firstName("Test")
        .lastName("User")
        .email("test@example.com")
        .mobileNumber("0400000000")
        .addressLine1("123 Test St")
        .city("Sydney")
        .state("NSW")
        .postcode("2000")
        .country("Australia")
        .build();
  }
}
