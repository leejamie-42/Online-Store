package com.comp5348.store.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {

    private Long orderId;

    private List<OrderProductDto> products;

    private BigDecimal totalAmount;

    private String status;

    private String customerName;

    private LocalDateTime createdAt;
}
