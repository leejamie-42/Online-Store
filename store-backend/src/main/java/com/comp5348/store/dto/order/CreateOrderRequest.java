package com.comp5348.store.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new order.
 *
 * <p>Request Body Example:</p>
 * <pre>
 * {
 *   "product_id": 1,
 *   "quantity": 2,
 *   "user_id": 1,
 *   "shipping_info": {
 *     "first_name": "John",
 *     "last_name": "Doe",
 *     "email": "john@example.com",
 *     "mobile_number": "0400000000",
 *     "address_line1": "123 Main St",
 *     "city": "Sydney",
 *     "state": "NSW",
 *     "postcode": "2000",
 *     "country": "Australia"
 *   }
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Shipping information is required")
    @Valid
    private ShippingInfoDto shippingInfo;
}
