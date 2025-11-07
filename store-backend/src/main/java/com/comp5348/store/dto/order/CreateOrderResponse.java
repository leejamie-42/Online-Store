package com.comp5348.store.dto.order;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for order creation.
 *
 * <p>Response Body Example:</p>
 * <pre>
 * {
 *   "order_id": 1,
 *   "status": "PENDING",
 *   "total": 199.98
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private Long orderId;

    private String status;

    private BigDecimal total;
}
