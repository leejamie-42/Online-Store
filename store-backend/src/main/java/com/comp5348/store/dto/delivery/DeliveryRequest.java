package com.comp5348.store.dto.delivery;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private String orderId;
    private List<DeliveryPackageDto> deliveryPackages;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private String recipientEmail;
    private Integer packageCount;
    private BigDecimal declaredValue;
}
