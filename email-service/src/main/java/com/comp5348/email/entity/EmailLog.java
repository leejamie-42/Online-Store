package com.comp5348.email.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Records every email we send to customers
// Tracks email type (order confirmation, delivery update etc), who it went to, and if it worked
// retry_count lets us know if we had to resend it
@Entity
@Table(name = "email_log")
public class EmailLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "email_type", nullable = false, length = 50)
  private String emailType;

  @Column(name = "recipient", nullable = false)
  private String recipient;

  @Column(name = "subject")
  private String subject;

  @Column(name = "message_body", columnDefinition = "TEXT")
  private String messageBody;

  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount;

  // Constructors
  public EmailLog() {
    this.status = "PENDING";
    this.retryCount = 0;
    this.sentAt = LocalDateTime.now();
  }

  public EmailLog(Long orderId, String emailType, String recipient, String subject, String messageBody) {
    this();
    this.orderId = orderId;
    this.emailType = emailType;
    this.recipient = recipient;
    this.subject = subject;
    this.messageBody = messageBody;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getEmailType() {
    return emailType;
  }

  public void setEmailType(String emailType) {
    this.emailType = emailType;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMessageBody() {
    return messageBody;
  }

  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(Integer retryCount) {
    this.retryCount = retryCount;
  }
}
