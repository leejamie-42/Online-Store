package com.comp5348.deliveryco.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookRegistration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String event;  // "SHIPMENT_STATUS_UPDATE"

  @Column(nullable = false, length = 500)
  private String callbackUrl;  // "http://localhost:8080/api/webhooks/delivery"

  private LocalDateTime registeredAt;

  @PrePersist
  protected void onCreate() {
    registeredAt = LocalDateTime.now();
  }
}
