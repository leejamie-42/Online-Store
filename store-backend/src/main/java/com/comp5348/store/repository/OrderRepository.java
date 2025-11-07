package com.comp5348.store.repository;

import com.comp5348.store.model.order.Order;
import com.comp5348.store.model.order.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Order entity operations.
 *
 * <p>Provides methods for order data access with security and filtering.</p>
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific user, ordered by creation date descending.
     *
     * @param userId the user ID
     * @return list of orders ordered by most recent first
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find a specific order by ID and user ID (for security).
     *
     * <p>Ensures users can only access their own orders.</p>
     *
     * @param id the order ID
     * @param userId the user ID
     * @return Optional containing the order if found and belongs to user
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all orders with a specific status.
     *
     * <p>Useful for admin/fulfillment operations.</p>
     *
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Count total orders for a user.
     *
     * @param userId the user ID
     * @return total number of orders for the user
     */
    long countByUserId(Long userId);

    /**
     * Check if an order exists and belongs to a specific user.
     *
     * @param id the order ID
     * @param userId the user ID
     * @return true if order exists and belongs to user
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}
