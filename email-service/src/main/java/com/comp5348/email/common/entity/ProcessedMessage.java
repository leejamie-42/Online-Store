package com.comp5348.email.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Keeps track of which messages we've already handled
// RabbitMQ can send the same message multiple times, so we need to check
// if we've seen a message_id before. If yes, skip it.
// Both services use this table but with different service_name values
@Entity
@Table(name = "processed_messages")
public class ProcessedMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "message_id", nullable = false, unique = true)
  private String messageId;

  @Column(name = "service_name", nullable = false, length = 50)
  private String serviceName;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private LocalDateTime processedAt;

  // Constructors
  public ProcessedMessage() {
    this.processedAt = LocalDateTime.now();
  }

  public ProcessedMessage(String messageId, String serviceName) {
    this();
    this.messageId = messageId;
    this.serviceName = serviceName;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public LocalDateTime getProcessedAt() {
    return processedAt;
  }

  public void setProcessedAt(LocalDateTime processedAt) {
    this.processedAt = processedAt;
  }
}
