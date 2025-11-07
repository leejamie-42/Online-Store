package com.comp5348.store.model.order;

import com.comp5348.store.model.Product;
import com.comp5348.store.model.auth.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order entity representing customer orders with shipping information.
 *
 * <p>Relationships:</p>
 * <ul>
 *   <li>ManyToOne with User (order owner)</li>
 *   <li>ManyToOne with Product (ordered product)</li>
 * </ul>
 *
 * <p>Status Flow: PENDING → PROCESSING → PICKED_UP → DELIVERING → DELIVERED</p>
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_product_id", columnList = "product_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at"),
        @Index(
            name = "idx_orders_user_created",
            columnList = "user_id, created_at"
        ),
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_orders_user")
    )
    private User user;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_orders_product")
    )
    private Product product;

    // Order Details

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Shipping Information

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^0[2-9]\\d{8}$",
        message = "Mobile number must be a valid Australian mobile number (10 digits starting with 0)"
    )
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(
        regexp = "^(NSW|VIC|QLD|SA|WA|TAS|NT|ACT)$",
        message = "State must be a valid Australian state (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)"
    )
    @Column(nullable = false, length = 3)
    private String state;

    @NotBlank(message = "Postcode is required")
    @Pattern(regexp = "^\\d{4}$", message = "Postcode must be exactly 4 digits")
    @Column(nullable = false, length = 4)
    private String postcode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String country = "Australia";

    // Audit Fields

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Lifecycle Callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = OrderStatus.PENDING;
        }

        if (country == null || country.isBlank()) {
            country = "Australia";
        }

        // Calculate total amount if not set
        if (totalAmount == null && product != null && quantity != null) {
            totalAmount = product
                .getPrice()
                .multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business Methods

    /**
     * Calculate the total amount for this order.
     *
     * @return total amount (product price * quantity)
     */
    public BigDecimal calculateTotal() {
        if (product == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Check if this order can be cancelled.
     *
     * @return true if status is PENDING or PROCESSING
     */
    public boolean isCancellable() {
        return status != null && status.isCancellable();
    }

    /**
     * Check if this order is in a terminal state.
     *
     * @return true if status is DELIVERED or CANCELLED
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * Get the customer's full name.
     *
     * @return full name (first name + last name)
     */
    public String getCustomerFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get the full shipping address as a formatted string.
     *
     * @return formatted shipping address
     */
    public String getFormattedAddress() {
        return String.format(
            "%s, %s %s %s, %s",
            addressLine1,
            city,
            state,
            postcode,
            country
        );
    }
}
