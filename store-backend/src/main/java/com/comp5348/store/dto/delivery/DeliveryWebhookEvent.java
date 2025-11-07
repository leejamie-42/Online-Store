package com.comp5348.store.dto.delivery;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWebhookEvent {
    private String shipmentId;
    private String event; // SHIPMENT_CREATED, PICKED_UP, IN_TRANSIT, DELIVERED, LOST
    private LocalDateTime timestamp;
}

