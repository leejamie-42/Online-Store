package com.comp5348.store.dto.delivery;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private String shipmentId;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private String status;
}

