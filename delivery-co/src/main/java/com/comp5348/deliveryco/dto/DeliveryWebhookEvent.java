package com.comp5348.deliveryco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for delivery status webhook events sent to Store
 * Matches Store Backend's expected DeliveryWebhookEvent format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWebhookEvent {

  @JsonProperty("shipmentId")
  private String shipmentId;  // "SHIP-123"

  @JsonProperty("event")
  private String event;  // "PICKED_UP", "IN_TRANSIT", "DELIVERED", "LOST"

  @JsonProperty("timestamp")
  private LocalDateTime timestamp;
}
